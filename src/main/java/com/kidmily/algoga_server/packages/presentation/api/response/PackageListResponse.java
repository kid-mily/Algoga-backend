package com.kidmily.algoga_server.packages.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "패키지 목록 응답")
public record PackageListResponse(

        @Schema(description = "패키지 ID")
        Long packageId,

        @Schema(description = "패키지명")
        String name,

        @Schema(description = "항공사 코드")
        String airlineCode,

        @Schema(description = "항공사명")
        String airlineName,

        @Schema(description = "가는편 편명")
        String flightNumber,

        @Schema(description = "출발 공항")
        String departureAirport,

        @Schema(description = "도착 공항")
        String arrivalAirport,

        @Schema(description = "출발일")
        LocalDate departureDate,

        @Schema(description = "귀국일")
        LocalDate returnDate,

        @Schema(description = "도착 시간")
        LocalTime arrivalTime,

        @Schema(description = "소요 시간(분)")
        int durationMinutes,

        @Schema(description = "잔여 좌석")
        int seatsAvailable,

        @Schema(description = "숙소명")
        String accommodationName,

        @Schema(description = "총 가격")
        int totalPrice,

        @Schema(description = "항공 가격")
        int flightPrice,

        @Schema(description = "숙소 가격")
        int accommodationPrice
) {
}