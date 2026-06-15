package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public record DiagnosisResult(
        Long id,
        Long userId,
        Long countryId,
        int correctCount,
        int totalCount,
        int score,
        String level,
        LocalDateTime createdAt
) {
}
