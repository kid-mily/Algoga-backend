package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisResultRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataDiagnosisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiagnosisResultRepositoryAdapter implements DiagnosisResultRepository {

    private final SpringDataDiagnosisResultRepository springDataDiagnosisResultRepository;

    @Override
    public DiagnosisResult save(DiagnosisResult diagnosisResult) {
        DiagnosisResultJpaEntity entity = new DiagnosisResultJpaEntity(
                diagnosisResult.userId(),
                diagnosisResult.countryId(),
                diagnosisResult.correctCount(),
                diagnosisResult.totalCount(),
                diagnosisResult.score(),
                diagnosisResult.level()
        );

        return toDomain(springDataDiagnosisResultRepository.save(entity));
    }

    @Override
    public List<DiagnosisResult> findLatestResultsByCountry(Long userId) {
        return springDataDiagnosisResultRepository.findLatestByUserIdGroupByCountry(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }


    @Override
    public List<DiagnosisResult> findForAdmin(Long userId, Long countryId) {
        List<DiagnosisResultJpaEntity> entities;

        if (userId != null && countryId != null) {
            entities = springDataDiagnosisResultRepository.findByUserIdAndCountryIdOrderByCreatedAtDesc(userId, countryId);
        } else if (userId != null) {
            entities = springDataDiagnosisResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else if (countryId != null) {
            entities = springDataDiagnosisResultRepository.findByCountryIdOrderByCreatedAtDesc(countryId);
        } else {
            entities = springDataDiagnosisResultRepository.findAllByOrderByCreatedAtDesc();
        }

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    private DiagnosisResult toDomain(DiagnosisResultJpaEntity entity) {
        return new DiagnosisResult(
                entity.getId(),
                entity.getUserId(),
                entity.getCountryId(),
                entity.getCorrectCount(),
                entity.getTotalCount(),
                entity.getScore(),
                entity.getLevel(),
                entity.getCreatedAt()
        );
    }
}