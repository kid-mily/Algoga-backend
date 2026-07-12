package com.kidmily.algoga_server.quiz.presentation.response;

import com.kidmily.algoga_server.quiz.application.result.WrongQuizAnswerResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오답 문항 응답")
public record WrongQuizAnswerResponse(

        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "퀴즈 문제", example = "일본 오사카 여행 전 준비물로 가장 적절한 것은?")
        String question,

        @Schema(description = "사용자가 선택한 보기 번호", example = "2")
        Integer selectedOption,

        @Schema(description = "정답 보기 번호", example = "1")
        int correctOption,

        @Schema(description = "해설", example = "해외여행 시 여권은 필수 준비물입니다.")
        String explanation
) {

    public static WrongQuizAnswerResponse from(WrongQuizAnswerResult result) {
        return new WrongQuizAnswerResponse(
                result.quizId(),
                result.question(),
                result.selectedOption(),
                result.correctOption(),
                result.explanation()
        );
    }
}