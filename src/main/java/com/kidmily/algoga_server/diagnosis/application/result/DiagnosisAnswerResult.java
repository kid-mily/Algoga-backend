package com.kidmily.algoga_server.diagnosis.application.result;

public record DiagnosisAnswerResult(
        Long questionId,
        Integer selectedOption,
        Integer correctOption,
        boolean correct,
        String explanation
) {
}
