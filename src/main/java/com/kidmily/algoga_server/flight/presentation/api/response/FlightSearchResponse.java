package com.kidmily.algoga_server.flight.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "항공편 검색 응답")
public record FlightSearchResponse(

        @Schema(description = "항공편 번호", example = "KE001")
        String flightNumber,

        @Schema(description = "항공사", example = "대한항공")
        String airline,

        @Schema(description = "출발지 IATA 코드", example = "ICN")
        String departure,

        @Schema(description = "도착지 IATA 코드", example = "NRT")
        String arrival,

        @Schema(description = "출발 시각", example = "2026-07-01T09:00:00")
        LocalDateTime departureTime,

        @Schema(description = "도착 시각", example = "2026-07-01T11:30:00")
        LocalDateTime arrivalTime,

        @Schema(description = "비행 시간", example = "2h 30m")
        String duration,

        @Schema(description = "가격 (1인 기준)", example = "450000")
        int price
) {
    public static FlightSearchResponse from(
            com.kidmily.algoga_server.flight.domain.model.FlightInfo flightInfo) {
        return new FlightSearchResponse(
                flightInfo.getFlightNumber(),
                flightInfo.getAirline(),
                flightInfo.getDeparture(),
                flightInfo.getArrival(),
                flightInfo.getDepartureTime(),
                flightInfo.getArrivalTime(),
                flightInfo.getDuration(),
                flightInfo.getPrice()
        );
    }
}