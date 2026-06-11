package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.QuizResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin quiz response")
public record AdminQuizResponse(
        Long quizId,
        Long courseId,
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation
) {
    public static AdminQuizResponse from(QuizResult quiz) {
        return new AdminQuizResponse(
                quiz.quizId(),
                quiz.courseId(),
                quiz.question(),
                quiz.option1(),
                quiz.option2(),
                quiz.option3(),
                quiz.option4(),
                quiz.correctOption(),
                quiz.explanation()
        );
    }
}