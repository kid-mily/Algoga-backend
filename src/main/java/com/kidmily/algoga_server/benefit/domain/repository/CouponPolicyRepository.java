package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;

import java.util.List;
import java.util.Optional;

public interface CouponPolicyRepository {

    CouponPolicy save(CouponPolicy couponPolicy);

    List<CouponPolicy> findAll();

    List<CouponPolicy> findByCourseId(Long courseId);

    List<CouponPolicy> findActiveByCourseId(Long courseId);

    Optional<CouponPolicy> findById(Long couponPolicyId);

    Optional<CouponPolicy> findActiveByIdAndCourseId(Long couponPolicyId, Long courseId);

    void deactivate(CouponPolicy couponPolicy);

    boolean existsActiveByCourseId(Long courseId);
}
