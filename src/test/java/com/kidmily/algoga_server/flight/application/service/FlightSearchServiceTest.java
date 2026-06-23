package com.kidmily.algoga_server.flight.application.service;

import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import com.kidmily.algoga_server.flight.infrastructure.FlightApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightSearchServiceTest {

    @Mock private FlightApiClient flightApiClient;

    @InjectMocks
    private FlightSearchService flightSearchService;

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(flightSearchService);
    }

    @Test
    @DisplayName("항공편 검색 성공 - 결과 반환")
    void 항공편_검색_성공() {
        // given
        String destination = "ICN";
        LocalDate departureDate = LocalDate.of(2026, 7, 1);

        FlightInfo flight = FlightInfo.of(
                "KE001", "대한항공", "PUS", "ICN",
                LocalDateTime.of(2026, 7, 1, 9, 0),
                LocalDateTime.of(2026, 7, 1, 10, 0),
                "1h", 50000
        );
        when(flightApiClient.getDepartureFlights(destination, departureDate))
                .thenReturn(List.of(flight));

        // when
        List<FlightInfo> result = flightSearchService.searchFlights(destination, departureDate);

        // then
        assertFalse(result.isEmpty());
        assertEquals("KE001", result.get(0).getFlightNumber());
    }

    @Test
    @DisplayName("API 결과가 없으면 Mock 폴백 데이터를 반환한다")
    void 항공편_검색_결과_없으면_Mock폴백_반환() {
        // given : 외부 API가 빈 결과를 줌
        when(flightApiClient.getDepartureFlights(anyString(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        List<FlightInfo> result = flightSearchService.searchFlights("ICN", LocalDate.now());

        // then : graceful degradation — 빈 리스트가 아니라 Mock 데이터로 채워 반환
        assertFalse(result.isEmpty());
    }
}