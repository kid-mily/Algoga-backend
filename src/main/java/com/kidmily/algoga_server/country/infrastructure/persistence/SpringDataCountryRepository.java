package com.kidmily.algoga_server.country.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCountryRepository extends JpaRepository<CountryJpaEntity, Long> {

    List<CountryJpaEntity> findByIsActiveTrue();
}