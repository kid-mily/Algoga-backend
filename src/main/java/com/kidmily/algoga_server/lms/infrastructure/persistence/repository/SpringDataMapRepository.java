package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CountryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataMapRepository extends JpaRepository<CountryJpaEntity, Long> {

    List<CountryJpaEntity> findByActiveTrueOrderByContinentCodeAscNameAsc();

    List<CountryJpaEntity> findByContinentCodeIgnoreCaseAndActiveTrueOrderByNameAsc(String continentCode);

    Optional<CountryJpaEntity> findByIdAndActiveTrue(Long id);
}