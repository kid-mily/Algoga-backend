package com.kidmily.algoga_server.diagnosis.application.command;

public record UpdateDiagnosisQuestionCommand(
        String questionText,
        String option1,
        String option2,
        String option3,
        String option4,
        Integer correctOption,
        String explanation,
        Integer questionOrder,
        Boolean active
) {
}
