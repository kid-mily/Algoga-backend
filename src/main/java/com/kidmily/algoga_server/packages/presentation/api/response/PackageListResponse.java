package com.kidmily.algoga_server.packages.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "패키지 목록 응답")
public record PackageListResponse(

        @Schema(description = "패키지 ID", example = "1")
        Long packageId,

        @Schema(description = "패키지명", example = "도쿄 3박 4일 패키지")
        String name,

        @Schema(description = "항공사 코드", example = "KE")
        String airlineCode,

        @Schema(description = "항공사명", example = "대한항공")
        String airlineName,

        @Schema(description = "가는편 편명", example = "KE701")
        String flightNumber,

        @Schema(description = "출발 공항", example = "인천국제공항(ICN)")
        String departureAirport,

        @Schema(description = "도착 공항", example = "도쿄 나리타공항(NRT)")
        String arrivalAirport,

        @Schema(description = "출발일", example = "2026-07-01")
        LocalDate departureDate,

        @Schema(description = "귀국일", example = "2026-07-04")
        LocalDate returnDate,

        @Schema(description = "도착 시간", example = "12:00:00")
        LocalTime arrivalTime,

        @Schema(description = "소요 시간(분)", example = "180")
        int durationMinutes,

        @Schema(description = "잔여 좌석", example = "20")
        int seatsAvailable,

        @Schema(description = "숙소명", example = "신주쿠 그랜드 호텔")
        String accommodationName,

        @Schema(description = "총 가격", example = "1200000")
        int totalPrice,

        @Schema(description = "항공 가격", example = "200000")
        int flightPrice,

        @Schema(description = "숙소 가격", example = "400000")
        int accommodationPrice
) {
}