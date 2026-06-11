package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisAnswerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataDiagnosisAnswerRepository extends JpaRepository<DiagnosisAnswerJpaEntity, Long> {

    List<DiagnosisAnswerJpaEntity> findByResultIdOrderByIdAsc(Long resultId);
}