package com.kidmily.algoga_server.packages.application.service;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.packages.application.usecase.PackageQueryUseCase;
import com.kidmily.algoga_server.packages.domain.model.Package;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import com.kidmily.algoga_server.packages.exception.PackageErrorCode;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageDetailResponse;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PackageQueryService implements PackageQueryUseCase {

    private final PackageRepository packageRepository;

    @Override
    public List<PackageListResponse> getPackagesByCountry(Long countryId, String departureAirport,
                                                          String arrivalAirport, LocalDate departureDate,
                                                          LocalDate returnDate) {
        List<PackageListResponse> result = packageRepository
                .findByFilters(countryId, departureAirport, arrivalAirport, departureDate, returnDate)
                .stream()
                .map(p -> new PackageListResponse(
                        p.getId(),
                        p.getName(),
                        p.getAirlineCode(),
                        p.getAirlineName(),
                        p.getFlightNumber(),
                        p.getDepartureAirport(),
                        p.getArrivalAirport(),
                        p.getDepartureDate(),
                        p.getReturnDate(),
                        p.getArrivalTime(),
                        p.getDurationMinutes(),
                        p.getSeatsAvailable(),
                        p.getAccommodationName(),
                        p.getTotalPrice(),
                        p.getFlightPrice(),
                        p.getAccommodationPrice()
                ))
                .toList();

        if (result.isEmpty()) {
            log.warn("[PackageQueryService] 해당 조건의 패키지 데이터가 없습니다. - countryId: {}", countryId);
        }

        return result;
    }

    @Override
    public PackageDetailResponse getPackageDetail(Long packageId) {
        Package p = packageRepository.findById(packageId)
                .orElseThrow(() -> {
                    log.warn("[PackageQueryService] 패키지를 찾을 수 없음 - packageId: {}", packageId);
                    return new BusinessException(PackageErrorCode.PACKAGE_NOT_FOUND);
                });

        return new PackageDetailResponse(
                p.getId(),
                p.getCountryId(),
                p.getName(),
                p.getTotalPrice(),
                p.getDepositRate(),
                p.getDescription(),
                p.getBalancePrice(),
                p.getFlightPrice(),
                p.getAccommodationPrice(),
                p.getAirlineCode(),
                p.getAirlineName(),
                p.getFlightNumber(),
                p.getDepartureAirport(),
                p.getArrivalAirport(),
                p.getDepartureDate(),
                p.getReturnDate(),
                p.getArrivalTime(),
                p.getReturnFlightNumber(),
                p.getReturnDepartureTime(),
                p.getReturnArrivalTime(),
                p.getDurationMinutes(),
                p.getSeatsAvailable(),
                p.getAccommodationName(),
                p.getAccommodationAddress(),
                p.getNights(),
                p.getAccommodationImage()
        );
    }
}