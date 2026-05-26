package com.kidmily.algoga_server.flight.application.service;

import com.kidmily.algoga_server.flight.application.usecase.FlightSearchUseCase;
import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FlightSearchService implements FlightSearchUseCase {

    private static final String DEPARTURE = "ICN";

    @Override
    public List<FlightInfo> searchFlights(String destination, LocalDate departureDate) {
        log.info("[FlightSearchService] 항공편 조회 - destination: {}, date: {}", destination, departureDate);
        // TODO: 공공데이터포털 API 키 발급 후 실제 API 호출로 교체
        return getMockFlights(destination, departureDate);
    }

    private List<FlightInfo> getMockFlights(String destination, LocalDate departureDate) {
        return List.of(
                FlightInfo.of(
                        "KE" + destination + "01",
                        "대한항공",
                        DEPARTURE,
                        destination,
                        departureDate.atTime(9, 0),
                        departureDate.atTime(11, 30),
                        "2h 30m",
                        450000
                ),
                FlightInfo.of(
                        "OZ" + destination + "01",
                        "아시아나항공",
                        DEPARTURE,
                        destination,
                        departureDate.atTime(13, 0),
                        departureDate.atTime(15, 20),
                        "2h 20m",
                        420000
                ),
                FlightInfo.of(
                        "7C" + destination + "01",
                        "제주항공",
                        DEPARTURE,
                        destination,
                        departureDate.atTime(17, 30),
                        departureDate.atTime(19, 50),
                        "2h 20m",
                        280000
                )
        );
    }
}