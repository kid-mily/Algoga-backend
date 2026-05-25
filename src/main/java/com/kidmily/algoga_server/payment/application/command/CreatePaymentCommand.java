package com.kidmily.algoga_server.payment.application.command;

import com.kidmily.algoga_server.payment.domain.model.PaymentType;

public record CreatePaymentCommand(
        Long bookingId,
        Long userId,
        PaymentType paymentType,
        int amount,
        int usedMileage,
        Long usedCouponId,
        String portonePaymentId   // 프론트에서 PortOne SDK 결제 후 받은 ID
) {}