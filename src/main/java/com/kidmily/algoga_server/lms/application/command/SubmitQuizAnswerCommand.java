package com.kidmily.algoga_server.lms.application.command;

public record SubmitQuizAnswerCommand(
        Long quizId,
        Integer selectedOption
) {
}