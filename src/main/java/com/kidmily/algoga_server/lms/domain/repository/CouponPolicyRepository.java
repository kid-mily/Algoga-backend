package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.CouponPolicy;

import java.util.List;
import java.util.Optional;

public interface CouponPolicyRepository {

    CouponPolicy save(CouponPolicy couponPolicy);

    List<CouponPolicy> findByCourseId(Long courseId);

    List<CouponPolicy> findActiveByCourseId(Long courseId);

    Optional<CouponPolicy> findById(Long couponPolicyId);

    boolean existsActiveByCourseId(Long courseId);
}