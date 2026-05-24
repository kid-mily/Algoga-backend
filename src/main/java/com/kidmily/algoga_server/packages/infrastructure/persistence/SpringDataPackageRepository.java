package com.kidmily.algoga_server.packages.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataPackageRepository extends JpaRepository<PackageJpaEntity, Long> {

    @Query("SELECT p FROM PackageJpaEntity p WHERE p.countryId = :countryId AND p.isDeleted = false " +
            "AND (:departureAirport IS NULL OR p.departureAirport = :departureAirport) " +
            "AND (:arrivalAirport IS NULL OR p.arrivalAirport = :arrivalAirport) " +
            "AND (:departureDate IS NULL OR p.departureDate = :departureDate) " +
            "AND (:returnDate IS NULL OR p.returnDate = :returnDate)")
    List<PackageJpaEntity> findByFilters(
            @Param("countryId") Long countryId,
            @Param("departureAirport") String departureAirport,
            @Param("arrivalAirport") String arrivalAirport,
            @Param("departureDate") LocalDate departureDate,
            @Param("returnDate") LocalDate returnDate
    );

    Optional<PackageJpaEntity> findByIdAndIsDeletedFalse(Long id);
}