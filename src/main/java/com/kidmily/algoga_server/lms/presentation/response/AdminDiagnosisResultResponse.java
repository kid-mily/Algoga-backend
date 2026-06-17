package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.AdminDiagnosisResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Admin diagnosis result response")
public record AdminDiagnosisResultResponse(
        Long resultId,
        Long userId,
        Long countryId,
        Integer correctCount,
        Integer totalCount,
        Integer score,
        String level,
        LocalDateTime submittedAt
) {
    public static AdminDiagnosisResultResponse from(AdminDiagnosisResult result) {
        return new AdminDiagnosisResultResponse(
                result.resultId(),
                result.userId(),
                result.countryId(),
                result.correctCount(),
                result.totalCount(),
                result.score(),
                result.level(),
                result.submittedAt()
        );
    }
}
