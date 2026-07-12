package com.kidmily.algoga_server.diagnosis.application.command;

import java.util.List;

public record SubmitDiagnosisCommand(
        Long userId,
        Long countryId,
        List<SubmitDiagnosisAnswerCommand> answers
) {
}
