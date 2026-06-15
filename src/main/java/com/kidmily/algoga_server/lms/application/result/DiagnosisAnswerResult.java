package com.kidmily.algoga_server.lms.application.result;

public record DiagnosisAnswerResult(
        Long questionId,
        Integer selectedOption,
        Integer correctOption,
        boolean correct,
        String explanation
) {
}
