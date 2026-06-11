package com.kidmily.algoga_server.benefit.domain.model;

import java.time.LocalDateTime;

public class MileageHistory {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final Long managerId;
    private final int amount;
    private final String type;
    private final String reason;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiredAt;

    private static final int MILEAGE_VALID_YEARS = 1;

    private MileageHistory(
            Long id,
            Long userId,
            Long courseId,
            Long managerId,
            int amount,
            String type,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime expiredAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.managerId = managerId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }

    public static MileageHistory create(
            Long userId,
            Long courseId,
            int amount,
            String type,
            String reason
    ) {
        LocalDateTime createdAt = LocalDateTime.now();

        return new MileageHistory(
                null,
                userId,
                courseId,
                null,
                amount,
                type,
                reason,
                createdAt,
                expiresAt(type, createdAt)
        );
    }

    public static MileageHistory earn(
            Long userId,
            Long courseId,
            int amount,
            String reason
    ) {
        return create(userId, courseId, amount, "EARN", reason);
    }

    public static MileageHistory use(
            Long userId,
            Long courseId,
            int amount,
            String reason
    ) {
        return create(userId, courseId, amount, "USE", reason);
    }

    public static MileageHistory adminEarn(
            Long userId,
            Long managerId,
            int amount,
            String reason
    ) {
        LocalDateTime createdAt = LocalDateTime.now();

        return new MileageHistory(
                null,
                userId,
                null,
                managerId,
                amount,
                "EARN",
                reason,
                createdAt,
                expiresAt("EARN", createdAt)
        );
    }

    public static MileageHistory adminUse(
            Long userId,
            Long managerId,
            int amount,
            String reason
    ) {
        LocalDateTime createdAt = LocalDateTime.now();

        return new MileageHistory(
                null,
                userId,
                null,
                managerId,
                amount,
                "USE",
                reason,
                createdAt,
                expiresAt("USE", createdAt)
        );
    }

    public static MileageHistory withId(
            Long id,
            Long userId,
            Long courseId,
            Long managerId,
            int amount,
            String type,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime expiredAt
    ) {
        return new MileageHistory(
                id,
                userId,
                courseId,
                managerId,
                amount,
                type,
                reason,
                createdAt,
                expiredAt
        );
    }

    private static LocalDateTime expiresAt(String type, LocalDateTime createdAt) {
        if ("EARN".equalsIgnoreCase(type)) {
            return createdAt.plusYears(MILEAGE_VALID_YEARS);
        }

        return null;
    }

    public boolean isAvailableAt(LocalDateTime now) {
        return !"EARN".equalsIgnoreCase(type) || expiredAt == null || !expiredAt.isBefore(now);
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

    public Long getManagerId() {
        return managerId;
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

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}
