package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class CourseReward {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final int issuedCouponCount;
    private final Long mileageHistoryId;
    private final LocalDateTime rewardedAt;

    private CourseReward(
            Long id,
            Long userId,
            Long courseId,
            int issuedCouponCount,
            Long mileageHistoryId,
            LocalDateTime rewardedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.issuedCouponCount = issuedCouponCount;
        this.mileageHistoryId = mileageHistoryId;
        this.rewardedAt = rewardedAt;
    }

    public static CourseReward create(
            Long userId,
            Long courseId,
            int issuedCouponCount,
            Long mileageHistoryId
    ) {
        return new CourseReward(
                null,
                userId,
                courseId,
                issuedCouponCount,
                mileageHistoryId,
                LocalDateTime.now()
        );
    }

    public static CourseReward withId(
            Long id,
            Long userId,
            Long courseId,
            int issuedCouponCount,
            Long mileageHistoryId,
            LocalDateTime rewardedAt
    ) {
        return new CourseReward(
                id,
                userId,
                courseId,
                issuedCouponCount,
                mileageHistoryId,
                rewardedAt
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

    public int getIssuedCouponCount() {
        return issuedCouponCount;
    }

    public Long getMileageHistoryId() {
        return mileageHistoryId;
    }

    public LocalDateTime getRewardedAt() {
        return rewardedAt;
    }
}