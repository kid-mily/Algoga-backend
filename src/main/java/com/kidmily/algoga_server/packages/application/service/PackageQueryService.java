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
        FlightSearchResponse flightInfo = null;
        try {
            List<FlightInfo> flights = flightSearchUseCase.searchFlights(
                    travelPackage.getFlightDestination(), travelPackage.getCheckInDate());
            if (!flights.isEmpty()) {
                flightInfo = FlightSearchResponse.from(flights.get(0));
            }
        } catch (Exception e) {
            log.warn("[PackageQueryService] 항공편 실시간 조회 실패 - packageId: {}, error: {}",
                    travelPackage.getId(), e.getMessage());
        }
        return PackageResponse.of(travelPackage, flightInfo);
    }
}
