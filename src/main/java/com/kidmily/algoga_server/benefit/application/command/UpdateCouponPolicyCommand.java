package com.kidmily.algoga_server.benefit.application.command;

public record UpdateCouponPolicyCommand(
        Long courseId,
        Long couponPolicyId,
        String couponName,
        String discountType,
        int discountValue
) {
}
