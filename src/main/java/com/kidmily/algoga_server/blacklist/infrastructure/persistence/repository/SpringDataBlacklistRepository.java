package com.kidmily.algoga_server.blacklist.infrastructure.persistence.repository;

import com.kidmily.algoga_server.blacklist.domain.model.BlacklistStatus;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.entity.BlacklistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataBlacklistRepository extends JpaRepository<BlacklistJpaEntity, Long> {
    boolean existsByUserIdAndStatus(Long userId, BlacklistStatus status);
    Optional<BlacklistJpaEntity> findByUserIdAndStatus(Long userId, BlacklistStatus status);
}