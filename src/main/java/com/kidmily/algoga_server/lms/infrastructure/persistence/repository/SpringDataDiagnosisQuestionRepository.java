package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisQuestionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataDiagnosisQuestionRepository extends JpaRepository<DiagnosisQuestionJpaEntity, Long> {

    List<DiagnosisQuestionJpaEntity> findByCountryIdAndActiveTrueOrderByQuestionOrderAscIdAsc(Long countryId);

    List<DiagnosisQuestionJpaEntity> findByCountryIdOrderByQuestionOrderAscIdAsc(Long countryId);

    List<DiagnosisQuestionJpaEntity> findByIdIn(List<Long> ids);
}