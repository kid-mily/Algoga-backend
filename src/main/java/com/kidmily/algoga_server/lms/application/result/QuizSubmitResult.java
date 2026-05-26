package com.kidmily.algoga_server.lms.application.result;

import java.util.List;

public record QuizSubmitResult(
        Long userId,
        Long courseId,
        int totalCount,
        int correctCount,
        int score,
        List<WrongQuizAnswerResult> wrongAnswers
) {
}