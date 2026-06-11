package com.kidmily.algoga_server.payment.presentation.api.response;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long bookingId,
        Long courseId,
        Long userId,
        PaymentType paymentType,
        int amount,
        int usedMileage,
        Long usedCouponId,
        PaymentStatus status,
        String portonePaymentId,
        LocalDateTime createdAt,
        // 어드민 상세 필드 (null 가능)
        String userName,
        String productName,
        String paymentMethod
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getCourseId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getUsedMileage(),
                payment.getUsedCouponId(),
                payment.getStatus(),
                payment.getPortonePaymentId(),
                payment.getCreatedAt(),
                null, null, null
        );
    }

    public static PaymentResponse fromWithDetail(Payment payment, String userName, String productName, String paymentMethod) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getCourseId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getUsedMileage(),
                payment.getUsedCouponId(),
                payment.getStatus(),
                payment.getPortonePaymentId(),
                payment.getCreatedAt(),
                userName,
                productName,
                paymentMethod
        );
    }
}