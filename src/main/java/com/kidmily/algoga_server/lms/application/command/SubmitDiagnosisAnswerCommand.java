package com.kidmily.algoga_server.lms.application.command;

public record SubmitDiagnosisAnswerCommand(
        Long questionId,
        Integer selectedOption
) {
}
