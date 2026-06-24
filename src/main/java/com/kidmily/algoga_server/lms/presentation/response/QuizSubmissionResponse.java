package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.QuizSubmissionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "퀴즈 제출 결과 조회 응답")
public record QuizSubmissionResponse(

        @Schema(description = "퀴즈 제출 결과 ID", example = "1")
        Long submissionId,

        @Schema(description = "사용자 ID", example = "2")
        Long userId,

        @Schema(description = "강의 ID", example = "53")
        Long courseId,

        @Schema(description = "전체 문제 수", example = "5")
        int totalCount,

        @Schema(description = "정답 개수", example = "4")
        int correctCount,

        @Schema(description = "점수", example = "80")
        int score,

        @Schema(description = "제출 일시", example = "2026-06-24T13:30:00")
        LocalDateTime submittedAt
) {
    public static QuizSubmissionResponse from(QuizSubmissionResult result) {
        return new QuizSubmissionResponse(
                result.submissionId(),
                result.userId(),
                result.courseId(),
                result.totalCount(),
                result.correctCount(),
                result.score(),
                result.submittedAt()
        );
    }
}