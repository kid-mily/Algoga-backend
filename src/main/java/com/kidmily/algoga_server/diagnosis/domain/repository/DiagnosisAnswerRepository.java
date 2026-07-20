package com.kidmily.algoga_server.diagnosis.domain.repository;

import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisAnswer;

public interface DiagnosisAnswerRepository {

    DiagnosisAnswer save(DiagnosisAnswer diagnosisAnswer);

    void deleteByQuestionId(Long questionId);

    void deleteByResultId(Long resultId);
}
