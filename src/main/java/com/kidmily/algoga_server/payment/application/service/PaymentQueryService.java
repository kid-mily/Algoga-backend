package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.infrastructure.pdf.ConfirmationPdfGenerator;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentMonthlyDetailResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentStatsResponse;
import com.kidmily.algoga_server.payment.settings.cache.PaymentCacheType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
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
import java.time.YearMonth;
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
    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;

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

    @Cacheable(value = PaymentCacheType.Const.MY_PAYMENTS, key = "#userId")
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
                .map(this::enrichPayment)
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
            String[] columns = {"결제번호", "사용자명", "상품명", "결제금액", "결제수단", "결제일시"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PaymentResponse p : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.paymentId());
                row.createCell(1).setCellValue(p.userName() != null ? p.userName() : "");
                row.createCell(2).setCellValue(p.productName() != null ? p.productName() : "");
                row.createCell(3).setCellValue(p.amount());
                row.createCell(4).setCellValue(p.paymentMethod() != null ? p.paymentMethod() : "");
                row.createCell(5).setCellValue(p.createdAt().toString());
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

    @Cacheable(value = PaymentCacheType.Const.ADMIN_PAYMENT_STATS, key = "#year != null ? #year : 'all'")
    @Override
    public List<PaymentStatsResponse> getAdminPaymentStats(Integer year) {
        log.info("[PaymentQueryService] 어드민 월별 수익 통계 조회 - year: {}", year);

        List<Payment> allPayments = paymentRepository
                .findByCreatedAtBetween(LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());

        if (year != null) {
            allPayments = allPayments.stream()
                    .filter(p -> p.getCreatedAt().getYear() == year)
                    .toList();
        }

        Map<String, List<Payment>> successGrouped = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(p ->
                        p.getCreatedAt().getYear() + "-" + p.getCreatedAt().getMonthValue()));

        Map<String, List<Payment>> refundGrouped = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .collect(Collectors.groupingBy(p ->
                        p.getCreatedAt().getYear() + "-" + p.getCreatedAt().getMonthValue()));

        List<PaymentStatsResponse> stats = successGrouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int y = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]);
                    int totalAmount = entry.getValue().stream().mapToInt(Payment::getAmount).sum();
                    long count = entry.getValue().size();
                    int refundAmount = refundGrouped.getOrDefault(entry.getKey(), List.of())
                            .stream().mapToInt(Payment::getAmount).sum();
                    int netAmount = totalAmount - refundAmount;
                    return new PaymentStatsResponse(y, m, totalAmount, count, refundAmount, netAmount, null);
                })
                .sorted(Comparator.comparingInt(PaymentStatsResponse::year)
                        .thenComparingInt(PaymentStatsResponse::month))
                .toList();

        return applyGrowthRate(stats);
    }

    @Override
    public PaymentMonthlyDetailResponse getAdminPaymentStatsByMonth(int year, int month) {
        log.info("[PaymentQueryService] 어드민 월별 상세 통계 조회 - year: {}, month: {}", year, month);

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.atEndOfMonth().atTime(LocalTime.MAX);

        List<Payment> payments = paymentRepository.findByCreatedAtBetween(from, to);

        int totalAmount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToInt(Payment::getAmount).sum();
        int refundAmount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .mapToInt(Payment::getAmount).sum();
        int netAmount = totalAmount - refundAmount;
        long count = payments.stream().filter(p -> p.getStatus() == PaymentStatus.SUCCESS).count();

        Map<Integer, List<Payment>> byDay = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getCreatedAt().getDayOfMonth()));

        List<PaymentMonthlyDetailResponse.DailyStats> dailyStats = byDay.entrySet().stream()
                .map(entry -> {
                    int day = entry.getKey();
                    int daySales = entry.getValue().stream().filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                            .mapToInt(Payment::getAmount).sum();
                    int dayRefund = entry.getValue().stream().filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                            .mapToInt(Payment::getAmount).sum();
                    return new PaymentMonthlyDetailResponse.DailyStats(day, daySales, dayRefund, daySales - dayRefund);
                })
                .sorted(Comparator.comparingInt(PaymentMonthlyDetailResponse.DailyStats::day))
                .toList();

        // 전월 성장률 계산
        YearMonth prevYm = ym.minusMonths(1);
        LocalDateTime prevFrom = prevYm.atDay(1).atStartOfDay();
        LocalDateTime prevTo = prevYm.atEndOfMonth().atTime(LocalTime.MAX);
        int prevNet = paymentRepository.findByCreatedAtBetween(prevFrom, prevTo).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToInt(Payment::getAmount).sum();

        Double growthRate = prevNet > 0 ? Math.round((netAmount - prevNet) * 1000.0 / prevNet) / 10.0 : null;

        return new PaymentMonthlyDetailResponse(year, month, totalAmount, refundAmount, netAmount, count, growthRate, dailyStats);
    }

    private List<PaymentStatsResponse> applyGrowthRate(List<PaymentStatsResponse> sorted) {
        java.util.ArrayList<PaymentStatsResponse> result = new java.util.ArrayList<>(sorted);
        for (int i = 0; i < result.size(); i++) {
            PaymentStatsResponse cur = result.get(i);
            Double rate = null;
            if (i > 0) {
                int prevNet = result.get(i - 1).netAmount();
                if (prevNet > 0) {
                    rate = Math.round((cur.netAmount() - prevNet) * 1000.0 / prevNet) / 10.0;
                }
            }
            result.set(i, new PaymentStatsResponse(
                    cur.year(), cur.month(), cur.totalAmount(), cur.count(),
                    cur.refundAmount(), cur.netAmount(), rate));
        }
        return result;
    }

    private PaymentResponse enrichPayment(Payment payment) {
        // 결제 시점 스냅샷 우선(탈퇴/하드딜리트 후에도 이름 보존), 없으면(옛 결제건) live 조회 fallback
        String userName = payment.getUserName() != null
                ? payment.getUserName()
                : userRepository.findById(payment.getUserId())
                        .map(User::getName)
                        .orElse(null);

        String productName = null;
        if (payment.getCourseId() != null) {
            productName = courseRepository.findByIdAndDeletedFalse(payment.getCourseId())
                    .map(Course::getTitle)
                    .orElse(null);
        } else if (payment.getBookingId() != null) {
            productName = bookingRepository.findById(payment.getBookingId())
                    .flatMap(b -> accommodationRepository.findById(b.getAccommodationId()))
                    .map(Accommodation::getName)
                    .orElse(null);
        }

        return PaymentResponse.fromWithDetail(payment, userName, productName, payment.getPaymentMethod());
    }

    public int calculateLectureAmount(Long courseId, int usedMileage, Long usedCouponId, Long userId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.COURSE_NOT_FOUND));
        int basePrice = course.getPrice();

        int couponDiscount = 0;
        if (usedCouponId != null) {
            UserCoupon userCoupon = userCouponRepository.findById(usedCouponId)
                    .orElseThrow(() -> new BusinessException(BenefitErrorCode.COUPON_POLICY_NOT_FOUND));
            if ("PERCENT".equals(userCoupon.getDiscountType()) || "RATE".equals(userCoupon.getDiscountType())) {
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