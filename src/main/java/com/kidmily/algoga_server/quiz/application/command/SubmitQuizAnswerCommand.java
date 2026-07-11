package com.kidmily.algoga_server.quiz.application.command;

public record SubmitQuizAnswerCommand(
        Long quizId,
        Integer selectedOption
) {
}