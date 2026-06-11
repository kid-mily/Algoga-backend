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

    boolean existsByUserIdAndCouponPolicyId(Long userId, Long couponPolicyId);

    boolean existsByUserIdAndCouponName(Long userId, String couponName);

    @Modifying
    @Query("UPDATE UserCouponJpaEntity u SET u.status = 'USED', u.usedAt = :usedAt WHERE u.id = :id")
    void markUsed(@Param("id") Long id, @Param("usedAt") LocalDateTime usedAt);
}
