package com.kidmily.algoga_server.lms.application.result;

public record CouponPolicyStatisticsResult(
        Long couponPolicyId,
        String couponName,
        String discountType,
        int discountValue,
        Long courseId,
        String courseTitle,
        Long countryId,
        String countryName,
        int issuedCount,
        int usedCount,
        int expiredCount,
        int availableCount
) {
}