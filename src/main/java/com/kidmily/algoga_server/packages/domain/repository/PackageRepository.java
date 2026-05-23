package com.kidmily.algoga_server.packages.domain.repository;

import com.kidmily.algoga_server.packages.domain.model.Package;

import java.util.List;
import java.util.Optional;

public interface PackageRepository {
    List<Package> findByCountryId(Long countryId);

    Optional<Package> findById(Long packageId);
}
