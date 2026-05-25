package com.kidmily.algoga_server.packages.domain.repository;

import com.kidmily.algoga_server.packages.domain.model.Package;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PackageRepository {

    List<Package> findByFilters(Long countryId, String departureAirport,
                                String arrivalAirport, LocalDate departureDate,
                                LocalDate returnDate);

    Optional<Package> findById(Long packageId);
}