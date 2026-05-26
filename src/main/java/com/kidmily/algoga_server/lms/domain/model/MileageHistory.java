package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class MileageHistory {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final int amount;
    private final String type;
    private final String reason;
    private final LocalDateTime createdAt;

    private MileageHistory(
            Long id,
            Long userId,
            Long courseId,
            int amount,
            String type,
            String reason,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static MileageHistory earnCourseReward(
            Long userId,
            Long courseId,
            int amount
    ) {
        return new MileageHistory(
                null,
                userId,
                courseId,
                amount,
                "EARN",
                "강의 이수 및 퀴즈 완료 보상",
                LocalDateTime.now()
        );
    }

    public static MileageHistory withId(
            Long id,
            Long userId,
            Long courseId,
            int amount,
            String type,
            String reason,
            LocalDateTime createdAt
    ) {
        return new MileageHistory(
                id,
                userId,
                courseId,
                amount,
                type,
                reason,
                createdAt
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

    public int getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}