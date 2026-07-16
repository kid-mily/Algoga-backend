package com.kidmily.algoga_server.chatbot.presentation.internal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Python 역호출(Function Calling)에 응답하는 회원 데이터 DTO 모음.
 *
 * ⚠️ PII 최소화 원칙: 여권/탑승객 정보, PG 결제키, 카드번호, 회원 실명 등은 포함하지 않는다.
 * (이 데이터는 외부 LLM(Gemini)에게 전달되므로 최소한만 노출한다.)
 */
public final class InternalUserData {

    private InternalUserData() {}

    // ── 수강·진도율 ──
    public record Enrollments(List<EnrollmentItem> enrollments) {}

    public record EnrollmentItem(
            Long courseId,
            String courseTitle,
            int progressRate,
            String learningStatus,
            int completedChapterCount,
            int totalChapterCount,
            LocalDateTime accessExpiresAt
    ) {}

    // ── 결제·환불 ──
    public record Payments(List<PaymentItem> payments) {}

    public record PaymentItem(
            Long paymentId,
            Long courseId,
            Long bookingId,
            String paymentType,
            int amount,
            int usedMileage,
            String status,
            String paymentMethod,
            String productName,
            LocalDateTime createdAt
    ) {}

    // ── 쿠폰·마일리지 ──
    public record Benefits(MileageInfo mileage, List<CouponItem> coupons) {}

    public record MileageInfo(int totalMileage, int totalEarnedMileage, int totalUsedMileage) {}

    public record CouponItem(
            String couponName,
            String discountType,
            int discountValue,
            String status,
            boolean usable,
            LocalDateTime expiredAt
    ) {}

    // ── 예약·패키지 ──
    public record Bookings(List<BookingItem> bookings) {}

    public record BookingItem(
            String bookingNumber,
            String status,
            int totalPrice,
            int depositPrice,
            int balancePrice,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int nights,
            LocalDateTime createdAt
    ) {}
}
