package com.kidmily.algoga_server.quiz.application.command;

import java.util.List;

public record SubmitQuizCommand(
        Long userId,
        Long courseId,
        List<SubmitQuizAnswerCommand> answers
) {
}