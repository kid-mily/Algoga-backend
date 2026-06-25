package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;

import java.util.List;

public interface DiagnosisResultRepository {

    DiagnosisResult save(DiagnosisResult diagnosisResult);

    List<DiagnosisResult> findLatestResultsByCountry(Long userId);


    List<DiagnosisResult> findForAdmin(Long userId, Long countryId);
}