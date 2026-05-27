package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.MileageHistoryJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataMileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MileageHistoryRepositoryAdapter implements MileageHistoryRepository {

    private final SpringDataMileageHistoryRepository springDataMileageHistoryRepository;

    @Override
    public MileageHistory save(MileageHistory mileageHistory) {
        MileageHistoryJpaEntity entity = new MileageHistoryJpaEntity(
                mileageHistory.getUserId(),
                mileageHistory.getCourseId(),
                mileageHistory.getManagerId(),
                mileageHistory.getAmount(),
                mileageHistory.getType(),
                mileageHistory.getReason(),
                mileageHistory.getCreatedAt()
        );

        MileageHistoryJpaEntity savedEntity = springDataMileageHistoryRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<MileageHistory> findAll() {
        return springDataMileageHistoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MileageHistory> findByUserId(Long userId) {
        return springDataMileageHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MileageHistory toDomain(MileageHistoryJpaEntity entity) {
        return MileageHistory.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getManagerId(),
                entity.getAmount(),
                entity.getType(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}