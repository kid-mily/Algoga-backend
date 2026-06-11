package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.CouponPolicyResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Coupon policy response")
public record CouponPolicyResponse(
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
    public static CouponPolicyResponse from(CouponPolicyResult couponPolicy) {
        return new CouponPolicyResponse(
                couponPolicy.couponPolicyId(),
                couponPolicy.courseId(),
                couponPolicy.managerId(),
                couponPolicy.couponName(),
                couponPolicy.discountType(),
                couponPolicy.discountValue(),
                couponPolicy.validDays(),
                couponPolicy.active(),
                couponPolicy.createdAt(),
                couponPolicy.updatedAt()
        );
    }
}