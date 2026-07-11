package com.kidmily.algoga_server.quiz.presentation.response;

import com.kidmily.algoga_server.quiz.application.result.QuizResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 퀴즈 응답")
public record UserQuizResponse(
        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "퀴즈 문제")
        String question,

        @Schema(description = "1번 보기")
        String option1,

        @Schema(description = "2번 보기")
        String option2,

        @Schema(description = "3번 보기")
        String option3,

        @Schema(description = "4번 보기")
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
