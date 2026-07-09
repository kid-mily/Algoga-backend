package com.kidmily.algoga_server.passport.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPassportRepository extends JpaRepository<PassportJpaEntity, Long> {

    Optional<PassportJpaEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
