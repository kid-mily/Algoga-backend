package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.result.CouponPolicyStatisticsResult;
import com.kidmily.algoga_server.benefit.application.result.CouponStatisticsResult;
import com.kidmily.algoga_server.benefit.application.usecase.CouponStatisticsUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponStatisticsService implements CouponStatisticsUseCase {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final LmsCoursePort lmsCoursePort;

    @Override
    public CouponStatisticsResult getCouponStatistics(
            Long courseId,
            Long countryId
    ) {
        log.info("[Coupon Statistics Query] 쿠폰 통계 조회 요청. courseId={}, countryId={}",
                courseId, countryId);

        validateFilters(courseId, countryId);

        List<CouponPolicy> couponPolicies = couponPolicyRepository.findAll();
        List<UserCoupon> userCoupons = userCouponRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        List<CouponPolicyStatisticsResult> policyStatistics = new ArrayList<>();

        for (CouponPolicy couponPolicy : couponPolicies) {
            LmsCoursePort.CourseSummary courseSummary = lmsCoursePort.findCourseSummary(couponPolicy.getCourseId())
                    .orElse(null);

            if (courseSummary == null) {
                continue;
            }

            if (courseId != null && !courseSummary.courseId().equals(courseId)) {
                continue;
            }

            if (countryId != null && !courseSummary.countryId().equals(countryId)) {
                continue;
            }

            List<UserCoupon> couponsForPolicy = userCoupons.stream()
                    .filter(userCoupon -> couponPolicy.getId().equals(userCoupon.getCouponPolicyId()))
                    .toList();

            int issuedCount = couponsForPolicy.size();

            int usedCount = (int) couponsForPolicy.stream()
                    .filter(this::isUsed)
                    .count();

            int expiredCount = (int) couponsForPolicy.stream()
                    .filter(userCoupon -> isExpired(userCoupon, now))
                    .count();

            int availableCount = (int) couponsForPolicy.stream()
                    .filter(userCoupon -> isAvailable(userCoupon, now))
                    .count();

            policyStatistics.add(new CouponPolicyStatisticsResult(
                    couponPolicy.getId(),
                    couponPolicy.getCouponName(),
                    couponPolicy.getDiscountType(),
                    couponPolicy.getDiscountValue(),
                    courseSummary.courseId(),
                    courseSummary.courseTitle(),
                    courseSummary.countryId(),
                    courseSummary.countryName(),
                    issuedCount,
                    usedCount,
                    expiredCount,
                    availableCount
            ));
        }

        int totalPolicyCount = policyStatistics.size();
        int totalIssuedCouponCount = policyStatistics.stream()
                .mapToInt(CouponPolicyStatisticsResult::issuedCount)
                .sum();
        int totalUsedCouponCount = policyStatistics.stream()
                .mapToInt(CouponPolicyStatisticsResult::usedCount)
                .sum();
        int totalExpiredCouponCount = policyStatistics.stream()
                .mapToInt(CouponPolicyStatisticsResult::expiredCount)
                .sum();
        int totalAvailableCouponCount = policyStatistics.stream()
                .mapToInt(CouponPolicyStatisticsResult::availableCount)
                .sum();

        log.info("[Coupon Statistics Query] 쿠폰 통계 조회 완료. policyCount={}, issuedCount={}, usedCount={}, expiredCount={}, availableCount={}",
                totalPolicyCount,
                totalIssuedCouponCount,
                totalUsedCouponCount,
                totalExpiredCouponCount,
                totalAvailableCouponCount);

        return new CouponStatisticsResult(
                courseId,
                countryId,
                totalPolicyCount,
                totalIssuedCouponCount,
                totalUsedCouponCount,
                totalExpiredCouponCount,
                totalAvailableCouponCount,
                policyStatistics
        );
    }

    private void validateFilters(
            Long courseId,
            Long countryId
    ) {
        if (courseId != null && lmsCoursePort.findCourseSummary(courseId).isEmpty()) {
            log.warn("[Coupon Statistics Query] 쿠폰 통계 조회 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    courseId);
            throw new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND);
        }

        if (countryId != null && !lmsCoursePort.existsCountry(countryId)) {
            log.warn("[Coupon Statistics Query] 쿠폰 통계 조회 실패. 존재하지 않거나 비활성화된 국가입니다. countryId={}",
                    countryId);
            throw new BenefitException(BenefitErrorCode.COUNTRY_NOT_FOUND);
        }
    }

    private boolean isUsed(UserCoupon userCoupon) {
        return "USED".equalsIgnoreCase(userCoupon.getStatus());
    }

    private boolean isExpired(
            UserCoupon userCoupon,
            LocalDateTime now
    ) {
        if (isUsed(userCoupon)) {
            return false;
        }

        if ("EXPIRED".equalsIgnoreCase(userCoupon.getStatus())) {
            return true;
        }

        return userCoupon.getExpiredAt() != null && userCoupon.getExpiredAt().isBefore(now);
    }

    private boolean isAvailable(
            UserCoupon userCoupon,
            LocalDateTime now
    ) {
        return "ISSUED".equalsIgnoreCase(userCoupon.getStatus())
                && !isExpired(userCoupon, now);
    }
}