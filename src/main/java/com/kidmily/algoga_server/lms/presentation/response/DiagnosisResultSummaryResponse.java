package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.DiagnosisResultSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 진단평가 결과 목록 응답")
public record DiagnosisResultSummaryResponse(

        @Schema(description = "진단평가 결과 ID", example = "1")
        Long resultId,

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "국가명", example = "일본")
        String countryName,

        @Schema(description = "진단평가 제출 일시", example = "2026-06-24T12:30:00")
        LocalDateTime submittedAt,

        @Schema(description = "진단 레벨 코드", example = "BEGINNER")
        String level,

        @Schema(description = "진단 레벨명", example = "초급")
        String levelName,

        @Schema(description = "진단 점수", example = "60")
        Integer score
) {
    public static DiagnosisResultSummaryResponse from(DiagnosisResultSummary result) {
        return new DiagnosisResultSummaryResponse(
                result.resultId(),
                result.countryId(),
                result.countryName(),
                result.submittedAt(),
                result.level(),
                result.levelName(),
                result.score()
        );
    }
}