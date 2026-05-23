package com.kidmily.algoga_server.packages.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "패키지 상세 응답")
public record PackageDetailResponse(

        @Schema(description = "패키지 ID")
        Long packageId,

        @Schema(description = "국가 ID")
        Long countryId,

        @Schema(description = "패키지명")
        String name,

        @Schema(description = "총 가격")
        int totalPrice,

        @Schema(description = "예약금 비율")
        BigDecimal depositRate,

        @Schema(description = "패키지 설명")
        String description,

        @Schema(description = "잔금")
        int balancePrice,

        @Schema(description = "항공 가격")
        int flightPrice,

        @Schema(description = "숙소 가격")
        int accommodationPrice,

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

        @Schema(description = "오는편 편명")
        String returnFlightNumber,

        @Schema(description = "귀국 출발 시간")
        String returnDepartureTime,

        @Schema(description = "귀국 도착 시간")
        LocalTime returnArrivalTime,

        @Schema(description = "소요 시간(분)")
        int durationMinutes,

        @Schema(description = "잔여 좌석")
        int seatsAvailable,

        @Schema(description = "숙소명")
        String accommodationName,

        @Schema(description = "숙소 주소")
        String accommodationAddress,

        @Schema(description = "숙박 일수")
        int nights,

        @Schema(description = "숙소 이미지")
        String accommodationImage
) {
}