package com.kidmily.algoga_server.packages.application.usecase;

import com.kidmily.algoga_server.packages.presentation.api.response.PackageDetailResponse;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageListResponse;

import java.time.LocalDate;
import java.util.List;

public interface PackageQueryUseCase {

    List<PackageListResponse> getPackagesByCountry(Long countryId, String departureAirport,
                                                   String arrivalAirport, LocalDate departureDate,
                                                   LocalDate returnDate);

    PackageDetailResponse getPackageDetail(Long packageId);
}