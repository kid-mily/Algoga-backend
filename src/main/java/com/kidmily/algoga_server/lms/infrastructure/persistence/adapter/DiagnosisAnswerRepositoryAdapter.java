package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisAnswer;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisAnswerRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisAnswerJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataDiagnosisAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DiagnosisAnswerRepositoryAdapter implements DiagnosisAnswerRepository {

    private final SpringDataDiagnosisAnswerRepository springDataDiagnosisAnswerRepository;

    @Override
    public DiagnosisAnswer save(DiagnosisAnswer diagnosisAnswer) {
        DiagnosisAnswerJpaEntity entity = new DiagnosisAnswerJpaEntity(
                diagnosisAnswer.resultId(),
                diagnosisAnswer.questionId(),
                diagnosisAnswer.selectedOption(),
                diagnosisAnswer.correct()
        );

        DiagnosisAnswerJpaEntity savedEntity = springDataDiagnosisAnswerRepository.save(entity);
        return new DiagnosisAnswer(
                savedEntity.getId(),
                savedEntity.getResultId(),
                savedEntity.getQuestionId(),
                savedEntity.getSelectedOption(),
                savedEntity.isCorrect()
        );
    }

    @Override
    public void deleteByQuestionId(Long questionId) {
        springDataDiagnosisAnswerRepository.deleteByQuestionId(questionId);
    }
}
