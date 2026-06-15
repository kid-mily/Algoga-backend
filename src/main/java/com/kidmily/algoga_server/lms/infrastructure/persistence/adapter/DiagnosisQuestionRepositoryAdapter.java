package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisQuestion;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisQuestionRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisQuestionJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataDiagnosisQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiagnosisQuestionRepositoryAdapter implements DiagnosisQuestionRepository {

    private final SpringDataDiagnosisQuestionRepository springDataDiagnosisQuestionRepository;

    @Override
    public List<DiagnosisQuestion> findActiveByCountryId(Long countryId) {
        return springDataDiagnosisQuestionRepository.findByCountryIdAndActiveTrueOrderByQuestionOrderAscIdAsc(countryId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DiagnosisQuestion> findByIds(List<Long> ids) {
        return springDataDiagnosisQuestionRepository.findByIdIn(ids)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return springDataDiagnosisQuestionRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataDiagnosisQuestionRepository.deleteById(id);
    }

    private DiagnosisQuestion toDomain(DiagnosisQuestionJpaEntity entity) {
        return new DiagnosisQuestion(
                entity.getId(),
                entity.getCountryId(),
                entity.getQuestionText(),
                entity.getOption1(),
                entity.getOption2(),
                entity.getOption3(),
                entity.getOption4(),
                entity.getCorrectOption(),
                entity.getExplanation(),
                entity.getQuestionOrder(),
                entity.isActive()
        );
    }
}
