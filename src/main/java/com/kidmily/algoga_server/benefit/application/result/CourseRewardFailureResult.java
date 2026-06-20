package com.kidmily.algoga_server.benefit.application.result;

import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailure;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;

import java.time.LocalDateTime;

public record CourseRewardFailureResult(
        Long failureId,
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
    public static CourseRewardFailureResult from(CourseRewardFailure failure) {
        return new CourseRewardFailureResult(
                failure.getId(),
                failure.getUserId(),
                failure.getCourseId(),
                failure.getCompletionId(),
                failure.getStatus(),
                failure.getFailureReason(),
                failure.getRetryCount(),
                failure.getCreatedAt(),
                failure.getLastFailedAt(),
                failure.getResolvedAt()
        );
    }
}