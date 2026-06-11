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
    @DisplayName("항공편 검색 결과 없으면 빈 리스트 반환")
    void 항공편_검색_결과_없으면_빈리스트_반환() {
        // given
        when(flightApiClient.getDepartureFlights(anyString(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        List<FlightInfo> result = flightSearchService.searchFlights("ICN", LocalDate.now());

        // then
        assertTrue(result.isEmpty());
    }
}