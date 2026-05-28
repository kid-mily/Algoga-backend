package com.kidmily.algoga_server.lms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "진단평가 답안 요청")
public record DiagnosisAnswerRequest(

        @Schema(description = "진단평가 문제 ID", example = "1")
        @NotNull(message = "문제 ID는 필수입니다.")
        Long questionId,

        @Schema(description = "사용자가 선택한 보기 번호", example = "2")
        @NotNull(message = "선택한 답안은 필수입니다.")
        @Min(value = 1, message = "보기 번호는 1부터 4 사이여야 합니다.")
        @Max(value = 4, message = "보기 번호는 1부터 4 사이여야 합니다.")
        Integer selectedOption
) {
}