package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseCompletionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Course completion response")
public record CourseCompletionResponse(
        Long completionId,
        Long userId,
        Long courseId,
        String certificateCode,
        LocalDateTime completedAt
) {
    public static CourseCompletionResponse from(CourseCompletionResult courseCompletion) {
        return new CourseCompletionResponse(
                courseCompletion.completionId(),
                courseCompletion.userId(),
                courseCompletion.courseId(),
                courseCompletion.certificateCode(),
                courseCompletion.completedAt()
        );
    }
}