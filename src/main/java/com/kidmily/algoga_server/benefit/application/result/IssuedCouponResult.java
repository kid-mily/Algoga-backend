package com.kidmily.algoga_server.benefit.application.result;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;

import java.time.LocalDateTime;

public record IssuedCouponResult(
        Long userCouponId,
        Long couponPolicyId,
        String couponName,
        String discountType,
        int discountValue,
        String status,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt
) {
    public static IssuedCouponResult from(UserCoupon userCoupon) {
        return new IssuedCouponResult(
                userCoupon.getId(),
                userCoupon.getCouponPolicyId(),
                userCoupon.getCouponName(),
                userCoupon.getDiscountType(),
                userCoupon.getDiscountValue(),
                userCoupon.getStatus(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt()
        );
    }
}