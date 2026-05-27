package com.kidmily.algoga_server.benefit.domain.model;

import java.time.LocalDateTime;

public class CouponPolicy {

    private final Long id;
    private final Long courseId;
    private final Long managerId;
    private final String couponName;
    private final String discountType;
    private final int discountValue;
    private final int validDays;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CouponPolicy(
            Long id,
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
        this.id = id;
        this.courseId = courseId;
        this.managerId = managerId;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.validDays = validDays;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CouponPolicy create(
            Long courseId,
            Long managerId,
            String couponName,
            String discountType,
            int discountValue,
            int validDays
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new CouponPolicy(
                null,
                courseId,
                managerId,
                couponName,
                discountType,
                discountValue,
                validDays,
                true,
                now,
                now
        );
    }

    public static CouponPolicy withId(
            Long id,
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
        return new CouponPolicy(
                id,
                courseId,
                managerId,
                couponName,
                discountType,
                discountValue,
                validDays,
                active,
                createdAt,
                updatedAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getManagerId() {
        return managerId;
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

    public int getValidDays() {
        return validDays;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}