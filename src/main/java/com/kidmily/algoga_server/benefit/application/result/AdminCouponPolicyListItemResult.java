package com.kidmily.algoga_server.benefit.application.result;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;

import java.time.LocalDateTime;

public record AdminCouponPolicyListItemResult(
        Long couponPolicyId,
        Long courseId,
        String courseTitle,
        Long managerId,
        String couponName,
        String discountType,
        int discountValue,
        int validDays,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminCouponPolicyListItemResult from(CouponPolicy couponPolicy, String courseTitle) {
        return new AdminCouponPolicyListItemResult(
                couponPolicy.getId(),
                couponPolicy.getCourseId(),
                courseTitle,
                couponPolicy.getManagerId(),
                couponPolicy.getCouponName(),
                couponPolicy.getDiscountType(),
                couponPolicy.getDiscountValue(),
                couponPolicy.getValidDays(),
                couponPolicy.isActive(),
                couponPolicy.getCreatedAt(),
                couponPolicy.getUpdatedAt()
        );
    }
}
