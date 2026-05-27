package com.kidmily.algoga_server.lms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "강의별 쿠폰 정책 등록 요청")
public record CreateCouponPolicyRequest(

        @Schema(description = "쿠폰명", example = "오사카 강의 수료 할인 쿠폰")
        @NotBlank(message = "쿠폰명은 필수입니다.")
        String couponName,

        @Schema(description = "할인 타입. RATE는 정률, AMOUNT는 정액", example = "RATE")
        @NotBlank(message = "할인 타입은 필수입니다.")
        String discountType,

        @Schema(description = "할인 값. RATE면 퍼센트, AMOUNT면 금액", example = "10")
        @NotNull(message = "할인 값은 필수입니다.")
        @Positive(message = "할인 값은 0보다 커야 합니다.")
        Integer discountValue,

        @Schema(description = "쿠폰 유효기간 일수", example = "30")
        @NotNull(message = "쿠폰 유효기간은 필수입니다.")
        @Positive(message = "쿠폰 유효기간은 0보다 커야 합니다.")
        Integer validDays
) {
}