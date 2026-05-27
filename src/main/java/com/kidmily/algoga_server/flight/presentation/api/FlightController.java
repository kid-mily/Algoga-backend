package com.kidmily.algoga_server.flight.presentation.api;

import com.kidmily.algoga_server.flight.application.usecase.FlightSearchUseCase;
import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import com.kidmily.algoga_server.flight.presentation.api.response.FlightSearchResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/flights")
@RequiredArgsConstructor
@Tag(name = "Flight", description = "항공편 API")
public class FlightController {

    private final FlightSearchUseCase flightSearchUseCase;

    @GetMapping("/search")
    @Operation(summary = "항공편 검색", description = "ICN 출발 기준으로 항공편 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FlightSearchResponse>>> searchFlights(
            @Parameter(description = "도착지 IATA 코드", example = "NRT")
            @RequestParam String destination,

            @Parameter(description = "출발 날짜", example = "2026-07-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        List<FlightInfo> flights = flightSearchUseCase.searchFlights(destination, departureDate);
        List<FlightSearchResponse> response = flights.stream()
                .map(FlightSearchResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("FLIGHTS_FOUND", "항공편 조회에 성공했습니다.", response));
    }
}