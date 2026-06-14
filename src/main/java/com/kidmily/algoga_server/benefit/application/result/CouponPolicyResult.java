package com.kidmily.algoga_server.benefit.application.result;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;

import java.time.LocalDateTime;

public record CouponPolicyResult(
        Long couponPolicyId,
        Long courseId,
        Long managerId,
        String couponName,
        String discountType,
        int discountValue,
        int validDays,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponPolicyResult from(CouponPolicy couponPolicy) {
        return new CouponPolicyResult(
                couponPolicy.getId(),
                couponPolicy.getCourseId(),
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