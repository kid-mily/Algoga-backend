package com.kidmily.algoga_server.diagnosis.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "진단평가 결과 요청")
public record DiagnosisResultRequest(

        @Schema(description = "사용자가 선택한 국가 ID", example = "1")
        @NotNull(message = "국가 ID는 필수입니다.")
        Long countryId,

        @Schema(description = "정답 개수", example = "3")
        @NotNull(message = "정답 개수는 필수입니다.")
        @Min(value = 0, message = "정답 개수는 0 이상이어야 합니다.")
        Integer correctCount,

        @Schema(description = "전체 문항 수", example = "5")
        @NotNull(message = "전체 문항 수는 필수입니다.")
        @Min(value = 1, message = "전체 문항 수는 1 이상이어야 합니다.")
        Integer totalCount
) {
}