package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataDiagnosisResultRepository extends JpaRepository<DiagnosisResultJpaEntity, Long> {

    Optional<DiagnosisResultJpaEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    List<DiagnosisResultJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}