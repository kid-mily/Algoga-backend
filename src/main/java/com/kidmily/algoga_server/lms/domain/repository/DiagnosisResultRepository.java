package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DiagnosisResultRepository {

    DiagnosisResult save(DiagnosisResult diagnosisResult);

    Optional<DiagnosisResult> findLatestByUserId(Long userId);

    Page<DiagnosisResult> findByUserId(Long userId, Pageable pageable);

    List<DiagnosisResult> findForAdmin(Long userId, Long countryId);
}