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
<<<<<<< HEAD
=======

    boolean existsByCourseIdAndCouponName(Long courseId, String couponName);

    boolean existsByCourseIdAndCouponNameAndIdNot(Long courseId, String couponName, Long couponPolicyId);

    Optional<CouponPolicy> updateBasicInfo(
            Long couponPolicyId,
            Long courseId,
            String couponName,
            String discountType,
            int discountValue,
            int validDays
    );

    boolean deactivate(Long couponPolicyId, Long courseId);
>>>>>>> 9e394e2220795389f2b87882ee1f5f7586ebffc6
}
