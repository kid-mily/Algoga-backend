package com.kidmily.algoga_server.diagnosis.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisQuestionRepository;
import com.kidmily.algoga_server.diagnosis.infrastructure.persistence.entity.DiagnosisQuestionJpaEntity;
import com.kidmily.algoga_server.diagnosis.infrastructure.persistence.repository.SpringDataDiagnosisQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiagnosisQuestionRepositoryAdapter implements DiagnosisQuestionRepository {

    private final SpringDataDiagnosisQuestionRepository springDataDiagnosisQuestionRepository;

    @Override
    public DiagnosisQuestion save(DiagnosisQuestion question) {
        DiagnosisQuestionJpaEntity entity = springDataDiagnosisQuestionRepository.findById(
                        question.id() == null ? -1L : question.id()
                )
                .map(existing -> {
                    existing.update(
                            question.questionText(),
                            question.option1(),
                            question.option2(),
                            question.option3(),
                            question.option4(),
                            question.correctOption(),
                            question.explanation(),
                            question.questionOrder(),
                            question.active()
                    );
                    return existing;
                })
                .orElseGet(() -> {
                    DiagnosisQuestionJpaEntity newEntity = new DiagnosisQuestionJpaEntity(
                            question.countryId(),
                            question.questionText(),
                            question.option1(),
                            question.option2(),
                            question.option3(),
                            question.option4(),
                            question.correctOption(),
                            question.explanation(),
                            question.questionOrder()
                    );
                    if (!question.active()) {
                        newEntity.deactivate();
                    }
                    return newEntity;
                });

        return toDomain(springDataDiagnosisQuestionRepository.save(entity));
    }

    @Override
    public Optional<DiagnosisQuestion> findById(Long id) {
        return springDataDiagnosisQuestionRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<DiagnosisQuestion> findActiveByCountryId(Long countryId) {
        return springDataDiagnosisQuestionRepository.findByCountryIdAndActiveTrueOrderByQuestionOrderAscIdAsc(countryId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DiagnosisQuestion> findByCountryId(Long countryId) {
        return springDataDiagnosisQuestionRepository.findByCountryIdOrderByQuestionOrderAscIdAsc(countryId)
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
