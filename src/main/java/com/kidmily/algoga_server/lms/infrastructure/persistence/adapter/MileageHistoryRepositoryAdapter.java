package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.MileageHistory;
import com.kidmily.algoga_server.lms.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.MileageHistoryJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataMileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MileageHistoryRepositoryAdapter implements MileageHistoryRepository {

    private final SpringDataMileageHistoryRepository springDataMileageHistoryRepository;

    @Override
    public MileageHistory save(MileageHistory mileageHistory) {
        MileageHistoryJpaEntity entity = new MileageHistoryJpaEntity(
                mileageHistory.getUserId(),
                mileageHistory.getCourseId(),
                mileageHistory.getAmount(),
                mileageHistory.getType(),
                mileageHistory.getReason(),
                mileageHistory.getCreatedAt()
        );

        MileageHistoryJpaEntity savedEntity = springDataMileageHistoryRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<MileageHistory> findById(Long mileageHistoryId) {
        return springDataMileageHistoryRepository.findById(mileageHistoryId)
                .map(this::toDomain);
    }

    @Override
    public List<MileageHistory> findByUserId(Long userId) {
        return springDataMileageHistoryRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MileageHistory> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataMileageHistoryRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MileageHistory toDomain(MileageHistoryJpaEntity entity) {
        return MileageHistory.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getAmount(),
                entity.getType(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}