package com.kidmily.algoga_server.diagnosis.domain.repository;

import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;

import java.util.List;
import java.util.Optional;

public interface DiagnosisQuestionRepository {

    DiagnosisQuestion save(DiagnosisQuestion question);

    Optional<DiagnosisQuestion> findById(Long id);

    List<DiagnosisQuestion> findActiveByCountryId(Long countryId);

    List<DiagnosisQuestion> findByCountryId(Long countryId);

    List<DiagnosisQuestion> findByIds(List<Long> ids);

    boolean existsById(Long id);

    void deleteById(Long id);
}
