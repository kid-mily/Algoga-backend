package com.kidmily.algoga_server.diagnosis.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisResult;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisResultRepository;
import com.kidmily.algoga_server.diagnosis.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import com.kidmily.algoga_server.diagnosis.infrastructure.persistence.repository.SpringDataDiagnosisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<DiagnosisResult> findForAdmin(Long userId, Long countryId, Pageable pageable) {
        Page<DiagnosisResultJpaEntity> entities;

        if (userId != null && countryId != null) {
            entities = springDataDiagnosisResultRepository.findByUserIdAndCountryIdOrderByCreatedAtDesc(userId, countryId, pageable);
        } else if (userId != null) {
            entities = springDataDiagnosisResultRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else if (countryId != null) {
            entities = springDataDiagnosisResultRepository.findByCountryIdOrderByCreatedAtDesc(countryId, pageable);
        } else {
            entities = springDataDiagnosisResultRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return entities.map(this::toDomain);
    }

    @Override
    public List<Long> findIdsByUserId(Long userId) {
        return springDataDiagnosisResultRepository.findIdsByUserId(userId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataDiagnosisResultRepository.deleteByUserId(userId);
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