package com.kidmily.algoga_server.packages.domain.repository;

import com.kidmily.algoga_server.packages.domain.model.TravelPackage;

import java.util.List;
import java.util.Optional;

public interface PackageRepository {
    TravelPackage save(TravelPackage travelPackage);
    Optional<TravelPackage> findById(Long id);
    List<TravelPackage> findAll();
    List<TravelPackage> findByCountryId(Long countryId);
    void deleteById(Long id);
}
