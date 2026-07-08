package com.kidmily.algoga_server.packages.application.service;

import com.kidmily.algoga_server.flight.application.usecase.FlightSearchUseCase;
import com.kidmily.algoga_server.flight.domain.model.FlightInfo;
import com.kidmily.algoga_server.flight.presentation.api.response.FlightSearchResponse;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.packages.application.usecase.PackageQueryUseCase;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import com.kidmily.algoga_server.packages.exception.PackageErrorCode;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PackageQueryService implements PackageQueryUseCase {

    private final PackageRepository packageRepository;
    private final FlightSearchUseCase flightSearchUseCase;

    @Override
    public List<PackageResponse> getAll() {
        return packageRepository.findAll()
                .stream()
                .map(this::toResponseWithFlight)
                .toList();
    }

    @Override
    public List<PackageResponse> getByCountry(Long countryId) {
        return packageRepository.findByCountryId(countryId)
                .stream()
                .map(this::toResponseWithFlight)
                .toList();
    }

    @Override
    public PackageResponse getById(Long packageId) {
        TravelPackage travelPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException(PackageErrorCode.PACKAGE_NOT_FOUND));
        return toResponseWithFlight(travelPackage);
    }

    private PackageResponse toResponseWithFlight(TravelPackage travelPackage) {
        long nights = ChronoUnit.DAYS.between(travelPackage.getCheckInDate(), travelPackage.getCheckOutDate());

        FlightSearchResponse outbound = null;
        FlightSearchResponse returnFlight = null;
        try {
            List<FlightInfo> flights = flightSearchUseCase.searchFlights(
                    travelPackage.getFlightDestination(), travelPackage.getCheckInDate());
            FlightInfo picked = pickByAirline(flights, travelPackage.getAirline());
            if (picked != null) {
                outbound = FlightSearchResponse.from(picked);
                returnFlight = buildReturnFlight(outbound, travelPackage.getCheckOutDate());
            }
        } catch (Exception e) {
            log.warn("[PackageQueryService] 항공편 실시간 조회 실패 - packageId: {}, error: {}",
                    travelPackage.getId(), e.getMessage());
        }
        return PackageResponse.of(travelPackage, outbound, returnFlight, nights);
    }

    /**
     * 패키지에 저장된 항공사와 일치하는 운항편을 우선 선택한다.
     * 저장된 항공사가 없거나(구 데이터) 일치 편이 없으면 첫 운항편으로 폴백한다.
     */
    private FlightInfo pickByAirline(List<FlightInfo> flights, String airline) {
        if (flights == null || flights.isEmpty()) {
            return null;
        }
        if (airline != null && !airline.isBlank()) {
            return flights.stream()
                    .filter(f -> airline.equals(f.getAirline()))
                    .findFirst()
                    .orElse(flights.get(0));
        }
        return flights.get(0);
    }

    /**
     * 가는편을 뒤집어 오는편을 생성한다 (출발↔도착, checkOutDate 기준, 동일 항공사).
     * 공공 항공 API가 출국편만 제공해 귀국편 원천 데이터가 없으므로 시각/가격은 가는편 기준 추정값이다.
     */
    private FlightSearchResponse buildReturnFlight(FlightSearchResponse outbound, java.time.LocalDate checkOutDate) {
        if (outbound == null) {
            return null;
        }
        Duration flightDuration = Duration.between(outbound.departureTime(), outbound.arrivalTime());
        LocalDateTime returnDeparture = checkOutDate.atTime(outbound.departureTime().toLocalTime());
        LocalDateTime returnArrival = returnDeparture.plus(flightDuration);
        return new FlightSearchResponse(
                outbound.flightNumber() + "R",
                outbound.airline(),
                outbound.arrival(),
                outbound.departure(),
                returnDeparture,
                returnArrival,
                outbound.duration(),
                outbound.price()
        );
    }
}
