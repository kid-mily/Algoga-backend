package com.kidmily.algoga_server.quiz.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "퀴즈 제출 요청")
public record SubmitQuizRequest(

        @Schema(description = "제출 답안 목록")
        @NotEmpty(message = "퀴즈 답안은 최소 1개 이상 제출해야 합니다.")
        List<@Valid SubmitQuizAnswerRequest> answers
) {
}