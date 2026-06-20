package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.CourseRewardFailureResult;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 보상 실패 기록 응답")
public record CourseRewardFailureResponse(

        @Schema(description = "보상 실패 기록 ID", example = "1")
        Long failureId,

        @Schema(description = "사용자 ID", example = "10")
        Long userId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "수료 ID", example = "3")
        Long completionId,

        @Schema(description = "보상 실패 상태", example = "PENDING", allowableValues = {
                "PENDING", "RETRYING", "RESOLVED", "FAILED"
        })
        CourseRewardFailureStatus status,

        @Schema(description = "실패 사유", example = "강의 보상 지급 중 오류가 발생했습니다.")
        String failureReason,

        @Schema(description = "재시도 횟수", example = "1")
        int retryCount,

        @Schema(description = "최초 실패 기록 생성 일시", example = "2026-06-20T10:00:01")
        LocalDateTime createdAt,

        @Schema(description = "마지막 실패 일시", example = "2026-06-20T10:05:00")
        LocalDateTime lastFailedAt,

        @Schema(description = "처리 완료 일시", example = "2026-06-20T10:05:01")
        LocalDateTime resolvedAt
) {
    public static CourseRewardFailureResponse from(CourseRewardFailureResult result) {
        return new CourseRewardFailureResponse(
                result.failureId(),
                result.userId(),
                result.courseId(),
                result.completionId(),
                result.status(),
                result.failureReason(),
                result.retryCount(),
                result.createdAt(),
                result.lastFailedAt(),
                result.resolvedAt()
        );
    }
}