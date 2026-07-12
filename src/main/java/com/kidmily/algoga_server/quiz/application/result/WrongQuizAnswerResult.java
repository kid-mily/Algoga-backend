package com.kidmily.algoga_server.quiz.application.result;

public record WrongQuizAnswerResult(
        Long quizId,
        String question,
        Integer selectedOption,
        int correctOption,
        String explanation
) {
}