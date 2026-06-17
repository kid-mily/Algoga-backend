package com.kidmily.algoga_server.lms.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update diagnosis question request")
public record UpdateDiagnosisQuestionRequest(
        @NotBlank String questionText,
        @NotBlank String option1,
        @NotBlank String option2,
        @NotBlank String option3,
        @NotBlank String option4,
        @NotNull @Min(1) @Max(4) Integer correctOption,
        String explanation,
        @NotNull @Min(1) Integer questionOrder,
        Boolean active
) {
}
