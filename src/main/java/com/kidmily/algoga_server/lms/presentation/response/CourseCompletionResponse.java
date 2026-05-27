package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 이수 완료 응답")
public record CourseCompletionResponse(

        @Schema(description = "이수 완료 ID", example = "1")
        Long completionId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "수료증 고유 코드", example = "CERT-A1B2C3D4")
        String certificateCode,

        @Schema(description = "이수 완료 일시", example = "2026-05-25T23:10:00")
        LocalDateTime completedAt
) {

    public static CourseCompletionResponse from(CourseCompletion courseCompletion) {
        return new CourseCompletionResponse(
                courseCompletion.getId(),
                courseCompletion.getUserId(),
                courseCompletion.getCourseId(),
                courseCompletion.getCertificateCode(),
                courseCompletion.getCompletedAt()
        );
    }
}