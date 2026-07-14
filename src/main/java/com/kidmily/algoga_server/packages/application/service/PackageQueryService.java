package com.kidmily.algoga_server.packages.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PackageQueryService implements PackageQueryUseCase {

    private final PackageRepository packageRepository;
    private final FlightSearchUseCase flightSearchUseCase;
    private final AccommodationRepository accommodationRepository;
    private final CountryRepository countryRepository;

    @Override
    public List<PackageResponse> getAll() {
        return toResponses(packageRepository.findAll());
    }

    @Override
    public List<PackageResponse> getByCountry(Long countryId) {
        return toResponses(packageRepository.findByCountryId(countryId));
    }

    @Override
    public PackageResponse getById(Long packageId) {
        TravelPackage travelPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException(PackageErrorCode.PACKAGE_NOT_FOUND));

        Accommodation accommodation = travelPackage.getAccommodationId() == null ? null
                : accommodationRepository.findById(travelPackage.getAccommodationId()).orElse(null);
        String countryName = travelPackage.getCountryId() == null ? null
                : countryRepository.findById(travelPackage.getCountryId()).map(Country::getName).orElse(null);

        return toResponseWithFlight(travelPackage, accommodation, countryName);
    }

    /**
     * 목록 응답 변환. 숙소/국가를 패키지마다 개별 조회하면 N+1이 되므로,
     * 필요한 id를 모아 한 번에 조회(배치)한 뒤 각 패키지에 매핑한다.
     */
    private List<PackageResponse> toResponses(List<TravelPackage> packages) {
        List<Long> accommodationIds = packages.stream()
                .map(TravelPackage::getAccommodationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Accommodation> accommodationById = accommodationRepository.findByIdIn(accommodationIds)
                .stream()
                .collect(Collectors.toMap(Accommodation::getId, Function.identity()));

        List<Long> countryIds = packages.stream()
                .map(TravelPackage::getCountryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> countryNameById = countryRepository.findAllByIdIn(countryIds)
                .stream()
                .collect(Collectors.toMap(Country::getId, Country::getName));

        return packages.stream()
                .map(p -> toResponseWithFlight(
                        p,
                        accommodationById.get(p.getAccommodationId()),
                        countryNameById.get(p.getCountryId())))
                .toList();
    }

    private PackageResponse toResponseWithFlight(TravelPackage travelPackage,
                                                 Accommodation accommodation,
                                                 String countryName) {
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
        return PackageResponse.of(travelPackage, outbound, returnFlight, nights, accommodation, countryName);
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
