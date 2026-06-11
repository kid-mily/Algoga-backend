package com.kidmily.algoga_server.lms.domain.model;

public record DiagnosisQuestion(
        Long id,
        Long countryId,
        String questionText,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation,
        int questionOrder,
        boolean active
) {
}
