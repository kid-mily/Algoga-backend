package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class UserCoupon {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final Long couponPolicyId;
    private final String couponName;
    private final String discountType;
    private final int discountValue;
    private final String status;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiredAt;
    private final LocalDateTime usedAt;

    private UserCoupon(
            Long id,
            Long userId,
            Long courseId,
            Long couponPolicyId,
            String couponName,
            String discountType,
            int discountValue,
            String status,
            LocalDateTime issuedAt,
            LocalDateTime expiredAt,
            LocalDateTime usedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.couponPolicyId = couponPolicyId;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.status = status;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.usedAt = usedAt;
    }

    public static UserCoupon issue(
            Long userId,
            CouponPolicy couponPolicy
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new UserCoupon(
                null,
                userId,
                couponPolicy.getCourseId(),
                couponPolicy.getId(),
                couponPolicy.getCouponName(),
                couponPolicy.getDiscountType(),
                couponPolicy.getDiscountValue(),
                "ISSUED",
                now,
                now.plusDays(couponPolicy.getValidDays()),
                null
        );
    }

    public static UserCoupon withId(
            Long id,
            Long userId,
            Long courseId,
            Long couponPolicyId,
            String couponName,
            String discountType,
            int discountValue,
            String status,
            LocalDateTime issuedAt,
            LocalDateTime expiredAt,
            LocalDateTime usedAt
    ) {
        return new UserCoupon(
                id,
                userId,
                courseId,
                couponPolicyId,
                couponName,
                discountType,
                discountValue,
                status,
                issuedAt,
                expiredAt,
                usedAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getCouponPolicyId() {
        return couponPolicyId;
    }

    public String getCouponName() {
        return couponName;
    }

    public String getDiscountType() {
        return discountType;
    }

    public int getDiscountValue() {
        return discountValue;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
}