package com.kidmily.algoga_server.diagnosis.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "진단평가 제출 요청")
public record DiagnosisSubmitRequest(

        @Schema(description = "사용자가 선택한 국가 ID", example = "1")
        @NotNull(message = "국가 ID는 필수입니다.")
        Long countryId,

        @Schema(description = "사용자 답안 목록")
        @Valid
        @NotEmpty(message = "답안 목록은 필수입니다.")
        List<DiagnosisAnswerRequest> answers
) {
}