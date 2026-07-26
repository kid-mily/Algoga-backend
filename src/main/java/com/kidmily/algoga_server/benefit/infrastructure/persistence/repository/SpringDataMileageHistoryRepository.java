package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.MileageHistoryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataMileageHistoryRepository extends JpaRepository<MileageHistoryJpaEntity, Long> {

    List<MileageHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<MileageHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<MileageHistoryJpaEntity> findByUserIdIn(List<Long> userIds);

    @Query(
            value = "SELECT DISTINCT m.userId FROM MileageHistoryJpaEntity m ORDER BY m.userId ASC",
            countQuery = "SELECT COUNT(DISTINCT m.userId) FROM MileageHistoryJpaEntity m"
    )
    Page<Long> findDistinctUserIds(Pageable pageable);

    @Query("""
            SELECT
                COUNT(DISTINCT m.userId) AS userCount,
                COALESCE(SUM(CASE
                    WHEN UPPER(m.type) = 'EARN' AND (m.expiredAt IS NULL OR m.expiredAt >= :now)
                    THEN m.amount ELSE 0 END), 0) AS totalEarnedMileage,
                COALESCE(SUM(CASE
                    WHEN UPPER(m.type) IN ('USE', 'USED')
                    THEN m.amount ELSE 0 END), 0) AS totalUsedMileage
            FROM MileageHistoryJpaEntity m
            """)
    GlobalMileageTotalsProjection findGlobalTotals(@Param("now") LocalDateTime now);

    void deleteByUserId(Long userId);

    interface GlobalMileageTotalsProjection {
        long getUserCount();

        int getTotalEarnedMileage();

        int getTotalUsedMileage();
    }
}
