package com.kidmily.algoga_server.country.infrastructure.persistence.repository;

import com.kidmily.algoga_server.country.infrastructure.persistence.entity.CountryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCountryRepository extends JpaRepository<CountryJpaEntity, Long> {
    List<CountryJpaEntity> findByActiveTrue();
    List<CountryJpaEntity> findAllByIdIn(List<Long> ids);
}