package com.kidmily.algoga_server.completion.presentation.response;

import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 수료 응답")
public record CourseCompletionResponse(
        @Schema(description = "수료 내역 ID", example = "1")
        Long completionId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "수료증 코드", example = "ALG-2026-CERT-123456")
        String certificateCode,

        @Schema(description = "수료 일시", example = "2026-06-11T17:30:00")
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
