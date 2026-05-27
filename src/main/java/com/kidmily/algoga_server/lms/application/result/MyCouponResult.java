package com.kidmily.algoga_server.lms.application.result;

import java.time.LocalDateTime;

public record MyCouponResult(
        Long userCouponId,
        Long courseId,
        String courseTitle,
        Long couponPolicyId,
        String couponName,
        String discountType,
        int discountValue,
        String status,
        boolean usable,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt,
        LocalDateTime usedAt
) {
}