package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;

import java.time.LocalDateTime;

public record AdminDiagnosisResult(
        Long resultId,
        Long userId,
        Long countryId,
        Integer correctCount,
        Integer totalCount,
        Integer score,
        String level,
        LocalDateTime submittedAt
) {
    public static AdminDiagnosisResult from(DiagnosisResult result) {
        return new AdminDiagnosisResult(
                result.id(),
                result.userId(),
                result.countryId(),
                result.correctCount(),
                result.totalCount(),
                result.score(),
                result.level(),
                result.createdAt()
        );
    }
}
