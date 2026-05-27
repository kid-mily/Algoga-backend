package com.kidmily.algoga_server.flight.application.service;

import com.kidmily.algoga_server.flight.application.usecase.FlightSearchUseCase;
import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import com.kidmily.algoga_server.flight.infrastructure.FlightApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightSearchService implements FlightSearchUseCase {

    private static final String DEPARTURE = "ICN";
    private final FlightApiClient flightApiClient;

    private static final Map<String, String> CITY_TO_AIRPORT = Map.ofEntries(
            // 일본
            Map.entry("TYO", "NRT"),
            Map.entry("OSA", "KIX"),
            // 중국
            Map.entry("BJS", "PEK"),
            Map.entry("SHA", "PVG"),
            // 영국
            Map.entry("LON", "LHR"),
            // 프랑스
            Map.entry("PAR", "CDG"),
            // 이탈리아
            Map.entry("ROM", "FCO"),
            // 미국
            Map.entry("NYC", "JFK"),
            Map.entry("WAS", "IAD"),
            Map.entry("CHI", "ORD")
    );

    @Override
    public List<FlightInfo> searchFlights(String destination, LocalDate departureDate) {
        String airportCode = CITY_TO_AIRPORT.getOrDefault(destination.toUpperCase(), destination.toUpperCase());
        log.info("[FlightSearchService] 항공편 조회 - destination: {} -> airportCode: {}, date: {}", destination, airportCode, departureDate);
        try {
            List<FlightInfo> flights = flightApiClient.getDepartureFlights(airportCode, departureDate);
            if (!flights.isEmpty()) {
                log.info("[FlightSearchService] 실제 API 데이터 사용 - {}건", flights.size());
                return flights;
            }
            log.warn("[FlightSearchService] API 결과 없음, Mock 데이터 사용 - destination: {}", airportCode);
            return getMockFlights(airportCode, departureDate);
        } catch (Exception e) {
            log.warn("[FlightSearchService] API 호출 실패, Mock 데이터 사용 - error: {}", e.getMessage());
            return getMockFlights(airportCode, departureDate);
        }
    }

    private List<FlightInfo> getMockFlights(String destination, LocalDate departureDate) {
        return List.of(
                FlightInfo.of("KE" + destination + "01", "대한항공", DEPARTURE, destination,
                        departureDate.atTime(9, 0), departureDate.atTime(11, 30), "2h 30m", 450000),
                FlightInfo.of("OZ" + destination + "01", "아시아나항공", DEPARTURE, destination,
                        departureDate.atTime(13, 0), departureDate.atTime(15, 20), "2h 20m", 420000),
                FlightInfo.of("7C" + destination + "01", "제주항공", DEPARTURE, destination,
                        departureDate.atTime(17, 30), departureDate.atTime(19, 50), "2h 20m", 280000)
        );
    }
}