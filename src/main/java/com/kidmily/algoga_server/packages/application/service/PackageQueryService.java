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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PackageQueryService implements PackageQueryUseCase {

    // 목록 항공편 병렬 조회 최대 동시 스레드 수 (패키지 수가 이보다 적으면 그 수만큼만 생성)
    private static final int MAX_FLIGHT_THREADS = 16;

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

    /** 항공편 실시간 조회 키 — 같은 (목적지, 체크인일)이면 결과가 동일하다. */
    private record FlightKey(String destination, LocalDate checkInDate) {}

    /**
     * 목록 응답 변환. 숙소/국가를 패키지마다 개별 조회하면 N+1이 되므로,
     * 필요한 id를 모아 한 번에 조회(배치)한 뒤 각 패키지에 매핑한다.
     */
    private List<PackageResponse> toResponses(List<TravelPackage> packages) {
        if (packages.isEmpty()) {
            return List.of();
        }

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

        // 항공편은 (목적지, 체크인일) 단위로만 달라진다. 같은 키의 패키지가 여러 개여도 외부 API는 한 번만
        // 호출하도록 키를 중복 제거한 뒤 서로 다른 키만 병렬로 조회한다(기존엔 패키지마다 개별 호출 → 중복 호출).
        List<FlightKey> distinctKeys = packages.stream()
                .filter(p -> p.getFlightDestination() != null)
                .map(p -> new FlightKey(p.getFlightDestination(), p.getCheckInDate()))
                .distinct()
                .toList();
        Map<FlightKey, List<FlightInfo>> flightsByKey = fetchFlightsByKey(distinctKeys);

        // 각 패키지 응답 생성 — 외부 호출 없이 위에서 조회한 flightsByKey 를 공유해 매핑(원래 순서 보존).
        return packages.stream()
                .map(p -> buildResponse(
                        p,
                        p.getFlightDestination() == null ? null
                                : flightsByKey.get(new FlightKey(p.getFlightDestination(), p.getCheckInDate())),
                        accommodationById.get(p.getAccommodationId()),
                        countryNameById.get(p.getCountryId())))
                .toList();
    }

    /**
     * 서로 다른 (목적지, 체크인일) 키만 병렬로 실시간 조회한다.
     * IO-bound라 공용 풀(parallelStream) 대신 전용 스레드풀 사용. 조회 서비스는 무상태(외부 HTTP +
     * 서킷브레이커, thread-safe)라 병렬 안전하고, DB 접근도 없다(숙소/국가는 이미 배치 조회 완료).
     * 개별 키 실패는 그 키만 빈 결과로 흡수(safeSearch) → 해당 패키지 flightInfo=null.
     */
    private Map<FlightKey, List<FlightInfo>> fetchFlightsByKey(List<FlightKey> keys) {
        if (keys.isEmpty()) {
            return Map.of();
        }
        ExecutorService flightPool = Executors.newFixedThreadPool(Math.min(keys.size(), MAX_FLIGHT_THREADS));
        try {
            List<CompletableFuture<Map.Entry<FlightKey, List<FlightInfo>>>> futures = keys.stream()
                    .map(k -> CompletableFuture.supplyAsync(() -> Map.entry(k, safeSearch(k)), flightPool))
                    .toList();
            Map<FlightKey, List<FlightInfo>> result = new HashMap<>();
            for (CompletableFuture<Map.Entry<FlightKey, List<FlightInfo>>> future : futures) {
                Map.Entry<FlightKey, List<FlightInfo>> entry = future.join();
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        } finally {
            flightPool.shutdown();
        }
    }

    private List<FlightInfo> safeSearch(FlightKey key) {
        try {
            return flightSearchUseCase.searchFlights(key.destination(), key.checkInDate());
        } catch (Exception e) {
            log.warn("[PackageQueryService] 항공편 실시간 조회 실패 - destination: {}, date: {}, error: {}",
                    key.destination(), key.checkInDate(), e.getMessage());
            return List.of();
        }
    }

    private PackageResponse toResponseWithFlight(TravelPackage travelPackage,
                                                 Accommodation accommodation,
                                                 String countryName) {
        List<FlightInfo> flights = travelPackage.getFlightDestination() == null ? null
                : safeSearch(new FlightKey(travelPackage.getFlightDestination(), travelPackage.getCheckInDate()));
        return buildResponse(travelPackage, flights, accommodation, countryName);
    }

    /** 조회된 항공편 목록으로 응답을 만든다(외부 호출 없음). 저장 항공사 우선 매칭 + 오는편 생성. */
    private PackageResponse buildResponse(TravelPackage travelPackage,
                                          List<FlightInfo> flights,
                                          Accommodation accommodation,
                                          String countryName) {
        long nights = ChronoUnit.DAYS.between(travelPackage.getCheckInDate(), travelPackage.getCheckOutDate());

        FlightSearchResponse outbound = null;
        FlightSearchResponse returnFlight = null;
        FlightInfo picked = pickByAirline(flights, travelPackage.getAirline());
        if (picked != null) {
            outbound = FlightSearchResponse.from(picked);
            returnFlight = buildReturnFlight(outbound, travelPackage.getCheckOutDate());
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
