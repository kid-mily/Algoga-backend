package com.kidmily.algoga_server.accommodation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAccommodationRepository extends JpaRepository<AccommodationJpaEntity, Long> {
    List<AccommodationJpaEntity> findByCountryId(Long countryId);
}