package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.MileageHistoryJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataMileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                mileageHistory.getCreatedAt(),
                mileageHistory.getExpiredAt()
        );

        MileageHistoryJpaEntity savedEntity = springDataMileageHistoryRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<MileageHistory> findByUserId(Long userId) {
        return springDataMileageHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Page<MileageHistory> findByUserId(Long userId, Pageable pageable) {
        return springDataMileageHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<MileageHistory> findByUserIdIn(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return springDataMileageHistoryRepository.findByUserIdIn(userIds)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Page<Long> findDistinctUserIds(Pageable pageable) {
        return springDataMileageHistoryRepository.findDistinctUserIds(pageable);
    }

    @Override
    public GlobalMileageTotals findGlobalTotals(LocalDateTime now) {
        SpringDataMileageHistoryRepository.GlobalMileageTotalsProjection projection =
                springDataMileageHistoryRepository.findGlobalTotals(now);

        return new GlobalMileageTotals(
                projection.getUserCount(),
                projection.getTotalEarnedMileage(),
                projection.getTotalUsedMileage()
        );
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataMileageHistoryRepository.deleteByUserId(userId);
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
                entity.getCreatedAt(),
                entity.getExpiredAt()
        );
    }
}
