package com.kidmily.algoga_server.packages.infrastructure.persistence;

import com.kidmily.algoga_server.packages.domain.model.Package;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import com.kidmily.algoga_server.packages.infrastructure.mapper.PackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PackageRepositoryAdapter implements PackageRepository {

    private final SpringDataPackageRepository springDataPackageRepository;
    private final PackageMapper packageMapper;

    @Override
    public List<Package> findByFilters(Long countryId, String departureAirport,
                                       String arrivalAirport, LocalDate departureDate,
                                       LocalDate returnDate) {
        return springDataPackageRepository
                .findByFilters(countryId, departureAirport, arrivalAirport, departureDate, returnDate)
                .stream()
                .map(packageMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Package> findById(Long packageId) {
        return springDataPackageRepository
                .findByIdAndIsDeletedFalse(packageId)
                .map(packageMapper::toDomain);
    }
}