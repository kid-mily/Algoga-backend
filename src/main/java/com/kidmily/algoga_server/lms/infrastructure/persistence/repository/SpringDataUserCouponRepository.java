package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.UserCouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataUserCouponRepository extends JpaRepository<UserCouponJpaEntity, Long> {

    List<UserCouponJpaEntity> findByUserId(Long userId);

    List<UserCouponJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCouponPolicyId(Long userId, Long couponPolicyId);
}