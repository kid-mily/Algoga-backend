package com.kidmily.algoga_server.payment.domain.event;

import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        Long userId,
        String userEmail,
        String userName,
        String bookingNumber,
        Long courseId,
        PaymentType paymentType,
        int amount,
        LocalDateTime paidAt
) {}