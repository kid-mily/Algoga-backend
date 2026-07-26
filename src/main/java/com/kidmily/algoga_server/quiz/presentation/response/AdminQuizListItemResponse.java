package com.kidmily.algoga_server.quiz.presentation.response;

import com.kidmily.algoga_server.quiz.application.result.AdminQuizListItemResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 통합 퀴즈 목록 응답")
public record AdminQuizListItemResponse(
        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "퀴즈 문제")
        String question,

        @Schema(description = "1번 보기")
        String option1,

        @Schema(description = "2번 보기")
        String option2,

        @Schema(description = "3번 보기")
        String option3,

        @Schema(description = "4번 보기")
        String option4,

        @Schema(description = "정답 보기 번호", example = "1")
        int correctOption,

        @Schema(description = "정답 해설")
        String explanation
) {
    public static AdminQuizListItemResponse from(AdminQuizListItemResult quiz) {
        return new AdminQuizListItemResponse(
                quiz.quizId(),
                quiz.courseId(),
                quiz.courseTitle(),
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
