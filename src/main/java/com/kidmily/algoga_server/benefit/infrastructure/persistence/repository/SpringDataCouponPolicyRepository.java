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
<<<<<<< HEAD
=======

    boolean existsByCourseIdAndCouponName(Long courseId, String couponName);

    boolean existsByCourseIdAndCouponNameAndIdNot(Long courseId, String couponName, Long id);
>>>>>>> 9e394e2220795389f2b87882ee1f5f7586ebffc6
}
