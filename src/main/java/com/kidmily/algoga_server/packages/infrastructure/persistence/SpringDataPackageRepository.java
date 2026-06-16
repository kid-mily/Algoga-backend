package com.kidmily.algoga_server.packages.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPackageRepository extends JpaRepository<PackageJpaEntity, Long> {
    List<PackageJpaEntity> findByCountryId(Long countryId);
}
