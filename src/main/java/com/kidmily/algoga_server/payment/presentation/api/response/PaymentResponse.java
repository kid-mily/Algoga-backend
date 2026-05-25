package com.kidmily.algoga_server.payment.presentation.api.response;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long bookingId,
        Long userId,
        PaymentType paymentType,
        int amount,
        int usedMileage,
        Long usedCouponId,
        PaymentStatus status,
        String portonePaymentId,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getUsedMileage(),
                payment.getUsedCouponId(),
                payment.getStatus(),
                payment.getPortonePaymentId(),
                payment.getCreatedAt()
        );
    }
}