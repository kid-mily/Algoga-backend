package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.DiagnosisResult;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisResultRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataDiagnosisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
    public Optional<DiagnosisResult> findLatestByUserId(Long userId) {
        return springDataDiagnosisResultRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(this::toDomain);
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
