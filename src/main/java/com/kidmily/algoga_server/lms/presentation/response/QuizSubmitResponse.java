package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.QuizSubmitResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "퀴즈 제출 및 채점 응답")
public record QuizSubmitResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "전체 문제 수", example = "5")
        int totalCount,

        @Schema(description = "정답 수", example = "4")
        int correctCount,

        @Schema(description = "점수", example = "80")
        int score,

        @Schema(description = "오답 목록")
        List<WrongQuizAnswerResponse> wrongAnswers
) {

    public static QuizSubmitResponse from(QuizSubmitResult result) {
        return new QuizSubmitResponse(
                result.userId(),
                result.courseId(),
                result.totalCount(),
                result.correctCount(),
                result.score(),
                result.wrongAnswers()
                        .stream()
                        .map(WrongQuizAnswerResponse::from)
                        .toList()
        );
    }
}