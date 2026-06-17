package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;

import java.util.List;
import java.util.Optional;

public interface DiagnosisResultRepository {

    DiagnosisResult save(DiagnosisResult diagnosisResult);

    Optional<DiagnosisResult> findLatestByUserId(Long userId);

    List<DiagnosisResult> findForAdmin(Long userId, Long countryId);
}
