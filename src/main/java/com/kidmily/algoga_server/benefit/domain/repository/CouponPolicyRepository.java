package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CouponPolicyRepository {

    CouponPolicy save(CouponPolicy couponPolicy);

    List<CouponPolicy> findAll();

    List<CouponPolicy> findByCourseId(Long courseId);

    Page<CouponPolicy> searchForAdmin(Long courseId, Boolean active, String keyword, Pageable pageable);

    List<CouponPolicy> findActiveByCourseId(Long courseId);

    Optional<CouponPolicy> findById(Long couponPolicyId);

    Optional<CouponPolicy> findActiveByIdAndCourseId(Long couponPolicyId, Long courseId);

    void deactivate(CouponPolicy couponPolicy);

    boolean existsActiveByCourseId(Long courseId);

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
}
