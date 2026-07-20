package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    List<UserCoupon> findAll();

    List<UserCoupon> findByUserId(Long userId);

    List<UserCoupon> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCouponPolicyId(Long userId, Long couponPolicyId);

    boolean existsByUserIdAndCouponName(Long userId, String couponName);

    Optional<UserCoupon> findById(Long id);

    void markUsed(Long userCouponId, LocalDateTime usedAt);

    void deleteAllByUserId(Long userId);
}
