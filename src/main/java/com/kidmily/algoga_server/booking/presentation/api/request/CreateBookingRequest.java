package com.kidmily.algoga_server.booking.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "예약 생성 요청")
public record CreateBookingRequest(
        @Schema(description = "숙소 ID", example = "1")
        @NotNull(message = "숙소 ID는 필수입니다.")
        Long accommodationId,

        @Schema(description = "유저 ID", example = "1")
        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId,

        @Schema(description = "항공편 정보")
        @NotNull(message = "항공편 정보는 필수입니다.")
        FlightInfoRequest flightInfo,

        @Schema(description = "항공편 가격", example = "200000")
        @Positive
        int flightPrice,

        @Schema(description = "체크인 날짜", example = "2026-07-01")
        @NotNull
        LocalDate checkInDate,

        @Schema(description = "체크아웃 날짜", example = "2026-07-04")
        @NotNull
        LocalDate checkOutDate
) {}