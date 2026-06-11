package com.kidmily.algoga_server.lms.domain.model;

public record DiagnosisAnswer(
        Long id,
        Long resultId,
        Long questionId,
        int selectedOption,
        boolean correct
) {
    public static DiagnosisAnswer create(Long resultId, Long questionId, int selectedOption, boolean correct) {
        return new DiagnosisAnswer(null, resultId, questionId, selectedOption, correct);
    }
}
