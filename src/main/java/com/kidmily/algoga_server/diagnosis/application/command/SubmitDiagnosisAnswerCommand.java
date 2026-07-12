package com.kidmily.algoga_server.diagnosis.application.command;

public record SubmitDiagnosisAnswerCommand(
        Long questionId,
        Integer selectedOption
) {
}
