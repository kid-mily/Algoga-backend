package com.kidmily.algoga_server.diagnosis.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Update diagnosis question request")
public record UpdateDiagnosisQuestionRequest(
        @NotBlank @Size(max = 500, message = "진단평가 문제는 500자 이하로 입력해주세요.") String questionText,
        @NotBlank @Size(max = 200, message = "진단평가 보기는 200자 이하로 입력해주세요.") String option1,
        @NotBlank @Size(max = 200, message = "진단평가 보기는 200자 이하로 입력해주세요.") String option2,
        @NotBlank @Size(max = 200, message = "진단평가 보기는 200자 이하로 입력해주세요.") String option3,
        @NotBlank @Size(max = 200, message = "진단평가 보기는 200자 이하로 입력해주세요.") String option4,
        @NotNull @Min(1) @Max(4) Integer correctOption,
        @Size(max = 1000, message = "진단평가 해설은 1000자 이하로 입력해주세요.") String explanation,
        @NotNull @Min(1) Integer questionOrder,
        Boolean active
) {
}
