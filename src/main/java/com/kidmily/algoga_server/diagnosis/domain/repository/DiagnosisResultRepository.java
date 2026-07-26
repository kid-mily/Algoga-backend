package com.kidmily.algoga_server.diagnosis.domain.repository;

import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiagnosisResultRepository {

    DiagnosisResult save(DiagnosisResult diagnosisResult);

    List<DiagnosisResult> findLatestResultsByCountry(Long userId);


    Page<DiagnosisResult> findForAdmin(Long userId, Long countryId, Pageable pageable);

    List<Long> findIdsByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}