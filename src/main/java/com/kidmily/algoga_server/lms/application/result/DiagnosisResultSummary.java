package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;

import java.time.LocalDateTime;

public record DiagnosisResultSummary(
        Long resultId,
        Long countryId,
        String countryName,
        LocalDateTime submittedAt,
        String level,
        String levelName,
        Integer score
) {
    public static DiagnosisResultSummary from(
            DiagnosisResult result,
            String countryName,
            String levelName
    ) {
        return new DiagnosisResultSummary(
                result.id(),
                result.countryId(),
                countryName,
                result.createdAt(),
                result.level(),
                levelName,
                result.score()
        );
    }
}