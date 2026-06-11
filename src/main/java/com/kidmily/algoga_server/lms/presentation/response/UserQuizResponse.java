package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.QuizResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User quiz response")
public record UserQuizResponse(
        Long quizId,
        Long courseId,
        String question,
        String option1,
        String option2,
        String option3,
        String option4
) {
    public static UserQuizResponse from(QuizResult quiz) {
        return new UserQuizResponse(
                quiz.quizId(),
                quiz.courseId(),
                quiz.question(),
                quiz.option1(),
                quiz.option2(),
                quiz.option3(),
                quiz.option4()
        );
    }
}