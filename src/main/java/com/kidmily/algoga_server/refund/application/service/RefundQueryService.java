package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.application.usecase.RefundQueryUseCase;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.refund.exception.RefundErrorCode;
import com.kidmily.algoga_server.refund.presentation.api.response.RefundResponse;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundQueryService implements RefundQueryUseCase {

    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final AccommodationRepository accommodationRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<RefundResponse> getMyRefunds(Long userId) {
        log.warn("[RefundQueryService] 내 환불 목록 조회 - userId: {}", userId);
        return refundRepository.findAllByUserId(userId)
                .stream()
                .map(RefundResponse::from)
                .toList();
    }

    @Override
    public List<RefundResponse> getAllRefunds(RefundStatus status, String userName, String bookingNumber, String productName) {
        log.warn("[RefundQueryService] 전체 환불 목록 조회 - status: {}, userName: {}, bookingNumber: {}, productName: {}",
                status, userName, bookingNumber, productName);

        List<RefundRequest> refunds = status == null
                ? refundRepository.findAll()
                : refundRepository.findAllByStatus(status);

        return refunds.stream()
                .map(this::enrichRefund)
                .filter(r -> matchesSearchParams(r, userName, bookingNumber, productName))
                .toList();
    }

    @Override
    public RefundResponse getRefund(Long refundId) {
        log.info("[RefundQueryService] 환불 단건 조회 - refundId: {}", refundId);
        RefundRequest refund = refundRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.warn("[RefundQueryService] 환불 요청을 찾을 수 없음 - refundId: {}", refundId);
                    return new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
                });
        return enrichRefund(refund);
    }

    @Override
    public boolean hasActiveRefund(Long userId) {
        return refundRepository.existsByUserIdAndStatusIn(
                userId, List.of(RefundStatus.REQUESTED, RefundStatus.UNDER_REVIEW));
    }

    @Override
    public byte[] getRefundExcel(RefundStatus status) {
        log.info("[RefundQueryService] 환불 엑셀 다운로드 - status: {}", status);
        List<RefundResponse> refunds = getAllRefunds(status, null, null, null);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("환불내역");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"환불ID", "사용자명", "예약번호", "상품명", "결제금액", "환불금액", "결제수단", "상태", "신청일시"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (RefundResponse r : refunds) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.refundId());
                row.createCell(1).setCellValue(r.userName() != null ? r.userName() : "");
                row.createCell(2).setCellValue(r.bookingNumber() != null ? r.bookingNumber() : "");
                row.createCell(3).setCellValue(r.productName() != null ? r.productName() : "");
                row.createCell(4).setCellValue(r.paidAmount() != null ? r.paidAmount() : 0);
                row.createCell(5).setCellValue(r.amount());
                row.createCell(6).setCellValue(r.paymentMethod() != null ? r.paymentMethod() : "");
                row.createCell(7).setCellValue(r.status().name());
                row.createCell(8).setCellValue(r.createdAt().toString());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.warn("[RefundQueryService] 엑셀 생성 실패: {}", e.getMessage());
            throw new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
        }
    }

    private RefundResponse enrichRefund(RefundRequest refund) {
        // 환불 요청 시점 스냅샷 우선(탈퇴/하드딜리트 후에도 이름 보존), 없으면(옛 환불건) live 조회 fallback
        String userName = refund.getUserName() != null
                ? refund.getUserName()
                : userRepository.findById(refund.getUserId())
                        .map(User::getName)
                        .orElse(null);

        Payment payment = paymentRepository.findById(refund.getPaymentId()).orElse(null);
        int paidAmount = payment != null ? payment.getAmount() : 0;
        String paymentMethod = payment != null ? payment.getPaymentMethod() : null;

        Booking booking = bookingRepository.findById(refund.getBookingId()).orElse(null);
        String bookingNumber = booking != null ? booking.getBookingNumber() : null;
        var checkInDate = booking != null ? booking.getCheckInDate() : null;

        String productName = resolveProductName(payment, booking);

        return RefundResponse.fromWithDetail(
                refund, userName, productName, bookingNumber, checkInDate, paidAmount, paymentMethod);
    }

    private String resolveProductName(Payment payment, Booking booking) {
        if (payment != null && payment.getCourseId() != null) {
            return courseRepository.findByIdAndDeletedFalse(payment.getCourseId())
                    .map(Course::getTitle)
                    .orElse(null);
        }
        if (booking != null) {
            return accommodationRepository.findById(booking.getAccommodationId())
                    .map(Accommodation::getName)
                    .orElse(null);
        }
        return null;
    }

    private boolean matchesSearchParams(RefundResponse r, String userName, String bookingNumber, String productName) {
        if (userName != null && !userName.isBlank()) {
            if (r.userName() == null || !r.userName().contains(userName)) return false;
        }
        if (bookingNumber != null && !bookingNumber.isBlank()) {
            if (r.bookingNumber() == null || !r.bookingNumber().contains(bookingNumber)) return false;
        }
        if (productName != null && !productName.isBlank()) {
            if (r.productName() == null || !r.productName().contains(productName)) return false;
        }
        return true;
    }
}
