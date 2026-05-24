package com.kidmily.algoga_server.booking.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "예약 생성 요청")
public record CreateBookingRequest(

        @Schema(description = "패키지 ID", example = "1")
        @NotNull(message = "패키지 ID는 필수입니다.")
        Long packageId,

        @Schema(description = "유저 ID", example = "1")
        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId
) {
}