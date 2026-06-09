package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.infrastructure.pdf.ConfirmationPdfGenerator;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentQueryService implements PaymentQueryUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ConfirmationPdfGenerator confirmationPdfGenerator;
    private final CourseRepository courseRepository;
    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;

    @Override
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("[PaymentQueryService] 결제 정보를 찾을 수 없음 - paymentId: {}", paymentId);
                    return new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                });
        return PaymentResponse.from(payment);
    }

    @Override
    public byte[] getConfirmationPdf(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("[PaymentQueryService] PDF 생성 실패 - 결제 없음 - paymentId: {}", paymentId);
                    return new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                });

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> {
                    log.warn("[PaymentQueryService] PDF 생성 실패 - 예약 없음 - bookingId: {}", payment.getBookingId());
                    return new BusinessException(PaymentErrorCode.BOOKING_NOT_FOUND);
                });

        log.info("[PaymentQueryService] 확인서 PDF 생성 - paymentId: {}, bookingId: {}",
                paymentId, payment.getBookingId());

        return confirmationPdfGenerator.generate(payment, booking);
    }

    @Cacheable(value = "myPayments", key = "#userId")
    @Override
    public List<PaymentResponse> getMyPayments(Long userId) {
        log.info("[PaymentQueryService] 내 결제 내역 조회 - userId: {}", userId);
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    public List<PaymentResponse> getAdminPayments(LocalDate from, LocalDate to) {
        log.info("[PaymentQueryService] 어드민 결제 내역 조회 - from: {}, to: {}", from, to);
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);
        return paymentRepository.findByCreatedAtBetween(fromDt, toDt)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    public byte[] getAdminPaymentsExcel(LocalDate from, LocalDate to) {
        log.info("[PaymentQueryService] 어드민 결제 내역 엑셀 다운로드 - from: {}, to: {}", from, to);
        List<PaymentResponse> payments = getAdminPayments(from, to);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("결제내역");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"결제ID", "유저ID", "예약ID", "강의ID", "결제유형", "금액", "마일리지사용", "상태", "결제일시"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PaymentResponse p : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.paymentId());
                row.createCell(1).setCellValue(p.userId());
                row.createCell(2).setCellValue(p.bookingId() != null ? p.bookingId() : 0);
                row.createCell(3).setCellValue(p.courseId() != null ? p.courseId() : 0);
                row.createCell(4).setCellValue(p.paymentType().name());
                row.createCell(5).setCellValue(p.amount());
                row.createCell(6).setCellValue(p.usedMileage());
                row.createCell(7).setCellValue(p.status().name());
                row.createCell(8).setCellValue(p.createdAt().toString());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.warn("[PaymentQueryService] 엑셀 생성 실패: {}", e.getMessage());
            throw new BusinessException(PaymentErrorCode.EXCEL_GENERATION_FAILED);
        }
    }

    @Cacheable(value = "adminPaymentStats", key = "'all'")
    @Override
    public List<PaymentStatsResponse> getAdminPaymentStats() {
        log.info("[PaymentQueryService] 어드민 월별 수익 통계 조회");

        Map<String, List<Payment>> grouped = paymentRepository
                .findByCreatedAtBetween(LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(p ->
                        p.getCreatedAt().getYear() + "-" + p.getCreatedAt().getMonthValue()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int totalAmount = entry.getValue().stream().mapToInt(Payment::getAmount).sum();
                    long count = entry.getValue().size();
                    return new PaymentStatsResponse(year, month, totalAmount, count);
                })
                .sorted(Comparator.comparingInt(PaymentStatsResponse::year)
                        .thenComparingInt(PaymentStatsResponse::month))
                .toList();
    }

    public int calculateLectureAmount(Long courseId, int usedMileage, Long usedCouponId, Long userId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.COURSE_NOT_FOUND));
        int basePrice = course.getPrice();

        int couponDiscount = 0;
        if (usedCouponId != null) {
            UserCoupon userCoupon = userCouponRepository.findById(usedCouponId)
                    .orElseThrow(() -> new BusinessException(BenefitErrorCode.COUPON_POLICY_NOT_FOUND));
            if ("PERCENT".equals(userCoupon.getDiscountType())) {
                couponDiscount = basePrice * userCoupon.getDiscountValue() / 100;
            } else {
                couponDiscount = Math.min(userCoupon.getDiscountValue(), basePrice);
            }
        }

        if (usedMileage > 0) {
            List<MileageHistory> histories = mileageHistoryRepository.findByUserId(userId);
            int balance = histories.stream()
                    .mapToInt(h -> "EARN".equals(h.getType()) ? h.getAmount() : -h.getAmount())
                    .sum();
            if (balance < usedMileage) {
                throw new BusinessException(PaymentErrorCode.INSUFFICIENT_MILEAGE);
            }
        }

        return Math.max(0, basePrice - couponDiscount - usedMileage);
    }
}