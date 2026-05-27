package com.kidmily.algoga_server.lms.application.command;

public record CreateCouponPolicyCommand(
        Long courseId,
        Long managerId,
        String couponName,
        String discountType,
        int discountValue,
        int validDays
) {
}