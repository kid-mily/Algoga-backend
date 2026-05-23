package com.kidmily.algoga_server.packages.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataPackageRepository extends JpaRepository<PackageJpaEntity, Long> {

    List<PackageJpaEntity> findByCountryIdAndIsDeletedFalse(Long countryId);

    Optional<PackageJpaEntity> findByIdAndIsDeletedFalse(Long packageId);
}