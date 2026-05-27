package com.kidmily.algoga_server.lms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "퀴즈 답안 제출 항목")
public record SubmitQuizAnswerRequest(

        @Schema(description = "퀴즈 ID", example = "1")
        @NotNull(message = "퀴즈 ID는 필수입니다.")
        Long quizId,

        @Schema(description = "사용자가 선택한 보기 번호. 1부터 4 사이", example = "2")
        @NotNull(message = "선택한 답안은 필수입니다.")
        @Min(value = 1, message = "선택한 답안은 1 이상이어야 합니다.")
        @Max(value = 4, message = "선택한 답안은 4 이하이어야 합니다.")
        Integer selectedOption
) {
}