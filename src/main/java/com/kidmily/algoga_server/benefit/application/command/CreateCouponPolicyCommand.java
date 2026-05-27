package com.kidmily.algoga_server.benefit.application.command;

public record CreateCouponPolicyCommand(
        Long courseId,
        Long managerId,
        String couponName,
        String discountType,
        int discountValue,
        int validDays
) {
}