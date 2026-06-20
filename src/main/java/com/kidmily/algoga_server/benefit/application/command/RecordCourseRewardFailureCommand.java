package com.kidmily.algoga_server.benefit.application.command;

public record RecordCourseRewardFailureCommand(
        Long userId,
        Long courseId,
        Long completionId,
        String failureReason
) {
}