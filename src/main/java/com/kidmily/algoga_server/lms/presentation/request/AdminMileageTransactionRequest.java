package com.kidmily.algoga_server.lms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 마일리지 지급/회수 요청")
public record AdminMileageTransactionRequest(

        @Schema(description = "마일리지 금액", example = "1000")
        @Min(value = 1, message = "마일리지 금액은 1 이상이어야 합니다.")
        int amount,

        @Schema(description = "지급/회수 사유", example = "이벤트 참여 보상")
        @NotBlank(message = "사유는 필수입니다.")
        String reason
) {
}