package com.kidmily.algoga_server.completion.application.result;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;

import java.time.LocalDateTime;

public record CourseCompletionResult(
        Long completionId,
        Long userId,
        Long courseId,
        String certificateCode,
        LocalDateTime completedAt
) {
    public static CourseCompletionResult from(CourseCompletion courseCompletion) {
        return new CourseCompletionResult(
                courseCompletion.getId(),
                courseCompletion.getUserId(),
                courseCompletion.getCourseId(),
                courseCompletion.getCertificateCode(),
                courseCompletion.getCompletedAt()
        );
    }
}