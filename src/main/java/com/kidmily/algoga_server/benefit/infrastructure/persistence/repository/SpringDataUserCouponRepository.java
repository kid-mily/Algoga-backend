package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.UserCouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataUserCouponRepository extends JpaRepository<UserCouponJpaEntity, Long> {

    List<UserCouponJpaEntity> findByUserId(Long userId);

    List<UserCouponJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("""
            SELECT
                u.couponPolicyId AS couponPolicyId,
                COUNT(u.id) AS issuedCount,
                COALESCE(SUM(CASE WHEN UPPER(u.status) = 'USED' THEN 1 ELSE 0 END), 0) AS usedCount,
                COALESCE(SUM(CASE
                    WHEN UPPER(u.status) <> 'USED'
                     AND (UPPER(u.status) = 'EXPIRED' OR u.expiredAt < :now)
                    THEN 1 ELSE 0 END), 0) AS expiredCount,
                COALESCE(SUM(CASE
                    WHEN UPPER(u.status) = 'ISSUED' AND u.expiredAt >= :now
                    THEN 1 ELSE 0 END), 0) AS availableCount
            FROM UserCouponJpaEntity u
            WHERE u.couponPolicyId IN :couponPolicyIds
            GROUP BY u.couponPolicyId
            """)
    List<CouponUsageCountProjection> countByCouponPolicyIds(
            @Param("couponPolicyIds") List<Long> couponPolicyIds,
            @Param("now") LocalDateTime now
    );

    boolean existsByUserIdAndCouponPolicyId(Long userId, Long couponPolicyId);

    boolean existsByUserIdAndCouponName(Long userId, String couponName);

    @Modifying
    @Query("UPDATE UserCouponJpaEntity u SET u.status = 'USED', u.usedAt = :usedAt WHERE u.id = :id")
    void markUsed(@Param("id") Long id, @Param("usedAt") LocalDateTime usedAt);

    void deleteByUserId(Long userId);

    interface CouponUsageCountProjection {
        Long getCouponPolicyId();

        long getIssuedCount();

        long getUsedCount();

        long getExpiredCount();

        long getAvailableCount();
    }
}
