package com.kidmily.algoga_server.quiz.application.result;

import com.kidmily.algoga_server.lms.application.result.CourseCompletionResult;

import java.util.List;

public record QuizSubmitResult(
        Long userId,
        Long courseId,
        int totalCount,
        int correctCount,
        int score,
        boolean courseCompleted,
        CourseCompletionResult completion,
        List<WrongQuizAnswerResult> wrongAnswers
) {
}