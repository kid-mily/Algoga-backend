package com.kidmily.algoga_server.benefit.domain.model;

import java.time.LocalDateTime;

public class CourseRewardFailure {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final Long completionId;
    private final CourseRewardFailureStatus status;
    private final String failureReason;
    private final int retryCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastFailedAt;
    private final LocalDateTime resolvedAt;

    private CourseRewardFailure(
            Long id,
            Long userId,
            Long courseId,
            Long completionId,
            CourseRewardFailureStatus status,
            String failureReason,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime lastFailedAt,
            LocalDateTime resolvedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.completionId = completionId;
        this.status = status;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.lastFailedAt = lastFailedAt;
        this.resolvedAt = resolvedAt;
    }

    public static CourseRewardFailure create(
            Long userId,
            Long courseId,
            Long completionId,
            String failureReason
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new CourseRewardFailure(
                null,
                userId,
                courseId,
                completionId,
                CourseRewardFailureStatus.PENDING,
                failureReason,
                0,
                now,
                now,
                null
        );
    }

    public static CourseRewardFailure withId(
            Long id,
            Long userId,
            Long courseId,
            Long completionId,
            CourseRewardFailureStatus status,
            String failureReason,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime lastFailedAt,
            LocalDateTime resolvedAt
    ) {
        return new CourseRewardFailure(
                id,
                userId,
                courseId,
                completionId,
                status,
                failureReason,
                retryCount,
                createdAt,
                lastFailedAt,
                resolvedAt
        );
    }

    public CourseRewardFailure markRetrying() {
        return new CourseRewardFailure(
                id,
                userId,
                courseId,
                completionId,
                CourseRewardFailureStatus.RETRYING,
                failureReason,
                retryCount + 1,
                createdAt,
                LocalDateTime.now(),
                resolvedAt
        );
    }

    public CourseRewardFailure markRetryFailed(String failureReason, int maxRetryCount) {
        CourseRewardFailureStatus nextStatus = retryCount >= maxRetryCount
                ? CourseRewardFailureStatus.FAILED
                : CourseRewardFailureStatus.PENDING;

        return new CourseRewardFailure(
                id,
                userId,
                courseId,
                completionId,
                nextStatus,
                failureReason,
                retryCount,
                createdAt,
                LocalDateTime.now(),
                resolvedAt
        );
    }

    public CourseRewardFailure markResolved() {
        return new CourseRewardFailure(
                id,
                userId,
                courseId,
                completionId,
                CourseRewardFailureStatus.RESOLVED,
                failureReason,
                retryCount,
                createdAt,
                lastFailedAt,
                LocalDateTime.now()
        );
    }

    public boolean isResolved() {
        return status == CourseRewardFailureStatus.RESOLVED;
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

    public Long getCompletionId() {
        return completionId;
    }

    public CourseRewardFailureStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastFailedAt() {
        return lastFailedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}