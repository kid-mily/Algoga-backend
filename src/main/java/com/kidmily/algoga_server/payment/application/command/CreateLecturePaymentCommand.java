package com.kidmily.algoga_server.payment.application.command;

public record CreateLecturePaymentCommand(
        Long courseId,
        Long userId,
        int amount,
        int usedMileage,
        Long usedCouponId,
        String portonePaymentId
) {}