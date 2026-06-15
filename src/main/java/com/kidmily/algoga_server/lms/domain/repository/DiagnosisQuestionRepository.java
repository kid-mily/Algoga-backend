package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisQuestion;

import java.util.List;

public interface DiagnosisQuestionRepository {

    List<DiagnosisQuestion> findActiveByCountryId(Long countryId);

    List<DiagnosisQuestion> findByIds(List<Long> ids);
}
