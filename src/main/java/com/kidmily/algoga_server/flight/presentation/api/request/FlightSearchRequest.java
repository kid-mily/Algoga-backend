package com.kidmily.algoga_server.flight.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "항공편 검색 요청")
public record FlightSearchRequest(

        @Schema(description = "도착지 IATA 코드", example = "NRT")
        @NotBlank(message = "도착지를 입력해주세요.")
        String destination,

        @Schema(description = "출발 날짜", example = "2026-07-01")
        @NotNull(message = "출발 날짜를 입력해주세요.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate departureDate
) {
}