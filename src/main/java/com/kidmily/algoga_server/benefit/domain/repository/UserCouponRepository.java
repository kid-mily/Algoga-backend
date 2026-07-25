package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    List<UserCoupon> findAll();

    /**
     * 사용(USED) 상태이면서 사용 시각(usedAt)이 [from, endExclusive) 구간인 쿠폰만 조회한다.
     * (통계 전환율 계산용 — 과거엔 findAll() 후 메모리 필터라 user_coupons 전체를 로드했다)
     * usedAt 이 NULL 인 과거 데이터는 범위 조건에서 자연히 제외된다.
     */
    List<UserCoupon> findUsedInPeriod(LocalDateTime from, LocalDateTime endExclusive);

    List<UserCoupon> findByUserId(Long userId);

    List<UserCoupon> findByUserIdAndCourseId(Long userId, Long courseId);

    Map<Long, CouponUsageCount> countByCouponPolicyIds(List<Long> couponPolicyIds, LocalDateTime now);

    boolean existsByUserIdAndCouponPolicyId(Long userId, Long couponPolicyId);

    boolean existsByUserIdAndCouponName(Long userId, String couponName);

    Optional<UserCoupon> findById(Long id);

    void markUsed(Long userCouponId, LocalDateTime usedAt);

    void deleteAllByUserId(Long userId);

    record CouponUsageCount(
            Long couponPolicyId,
            long issuedCount,
            long usedCount,
            long expiredCount,
            long availableCount
    ) {
    }
}
