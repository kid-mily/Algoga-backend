package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CouponPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCouponPolicyRepository extends JpaRepository<CouponPolicyJpaEntity, Long> {

    List<CouponPolicyJpaEntity> findByCourseId(Long courseId);

    List<CouponPolicyJpaEntity> findByCourseIdAndActiveTrue(Long courseId);

    Optional<CouponPolicyJpaEntity> findByIdAndCourseIdAndActiveTrue(Long id, Long courseId);

    boolean existsByCourseIdAndActiveTrue(Long courseId);
}
