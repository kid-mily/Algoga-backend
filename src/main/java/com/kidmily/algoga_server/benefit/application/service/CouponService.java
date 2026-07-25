package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.command.IssueWelcomeCouponCommand;
import com.kidmily.algoga_server.benefit.application.command.UpdateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.result.CouponPolicyResult;
import com.kidmily.algoga_server.benefit.application.result.CouponPolicyStatisticsResult;
import com.kidmily.algoga_server.benefit.application.result.CouponStatisticsResult;
import com.kidmily.algoga_server.benefit.application.usecase.CouponUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService implements CouponUseCase {

    private static final String WELCOME_COUPON_NAME = "웰컴쿠폰";

    private final LmsCoursePort lmsCoursePort;
    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;

    @Override
    @Transactional
    public CouponPolicyResult createCouponPolicy(CreateCouponPolicyCommand command) {
        log.info("[Coupon Policy Command] 쿠폰 정책 등록 요청. courseId={}, managerId={}, couponName={}",
                command.courseId(), command.managerId(), command.couponName());

        validateCourse(command.courseId());
        validateCouponPolicy(
                command.discountType(),
                command.discountValue()
        );
        validateCouponNameNotDuplicated(command.courseId(), command.couponName());

        CouponPolicy couponPolicy = CouponPolicy.create(
                command.courseId(),
                command.managerId(),
                command.couponName(),
                command.discountType().toUpperCase(),
                command.discountValue()
        );

        CouponPolicy savedCouponPolicy = couponPolicyRepository.save(couponPolicy);

        log.info("[Coupon Policy Command] 쿠폰 정책 등록 완료. couponPolicyId={}, courseId={}, couponName={}",
                savedCouponPolicy.getId(),
                savedCouponPolicy.getCourseId(),
                savedCouponPolicy.getCouponName());

        return CouponPolicyResult.from(savedCouponPolicy);
    }

    @Override
    public List<CouponPolicyResult> getCouponPolicies(Long courseId) {
        log.info("[Coupon Policy Query] 강의별 쿠폰 정책 목록 조회 요청. courseId={}", courseId);

        validateCourse(courseId);

        List<CouponPolicy> couponPolicies = couponPolicyRepository.findActiveByCourseId(courseId);

        log.info("[Coupon Policy Query] 강의별 쿠폰 정책 목록 조회 완료. courseId={}, count={}",
                courseId, couponPolicies.size());

        return couponPolicies.stream()
                .map(CouponPolicyResult::from)
                .toList();
    }

    @Override
    @Transactional
    public CouponPolicyResult updateCouponPolicy(UpdateCouponPolicyCommand command) {
        log.info("[Coupon Policy Command] 쿠폰 정책 수정 요청. courseId={}, couponPolicyId={}, couponName={}",
                command.courseId(), command.couponPolicyId(), command.couponName());

        validateCourse(command.courseId());
        validateCouponPolicy(
                command.discountType(),
                command.discountValue()
        );
        validateCouponNameNotDuplicated(
                command.courseId(),
                command.couponName(),
                command.couponPolicyId()
        );

        CouponPolicy updatedCouponPolicy = couponPolicyRepository.updateBasicInfo(
                command.couponPolicyId(),
                command.courseId(),
                command.couponName(),
                command.discountType().toUpperCase(),
                command.discountValue(),
                CouponPolicy.DEFAULT_VALID_DAYS
        ).orElseThrow(() -> {
            log.warn("[Coupon Policy Command] 쿠폰 정책 수정 실패. 존재하지 않거나 비활성화된 쿠폰 정책입니다. courseId={}, couponPolicyId={}",
                    command.courseId(), command.couponPolicyId());
            return new BenefitException(BenefitErrorCode.COUPON_POLICY_NOT_FOUND);
        });

        log.info("[Coupon Policy Command] 쿠폰 정책 수정 완료. couponPolicyId={}, courseId={}, couponName={}",
                updatedCouponPolicy.getId(),
                updatedCouponPolicy.getCourseId(),
                updatedCouponPolicy.getCouponName());

        return CouponPolicyResult.from(updatedCouponPolicy);
    }

    @Override
    @Transactional
    public void deactivateCouponPolicy(Long courseId, Long couponPolicyId) {
        log.info("[Coupon Policy Command] 쿠폰 정책 비활성화 요청. courseId={}, couponPolicyId={}",
                courseId, couponPolicyId);

        validateCourse(courseId);

        boolean deactivated = couponPolicyRepository.deactivate(couponPolicyId, courseId);

        if (!deactivated) {
            log.warn("[Coupon Policy Command] 쿠폰 정책 비활성화 실패. 존재하지 않거나 이미 비활성화된 쿠폰 정책입니다. courseId={}, couponPolicyId={}",
                    courseId, couponPolicyId);
            throw new BenefitException(BenefitErrorCode.COUPON_POLICY_NOT_FOUND);
        }

        log.info("[Coupon Policy Command] 쿠폰 정책 비활성화 완료. courseId={}, couponPolicyId={}",
                courseId, couponPolicyId);
    }

    @Override
    public CouponStatisticsResult getCouponStatistics(
            Long courseId,
            Long countryId
    ) {
        log.info("[Coupon Statistics Query] 쿠폰 통계 조회 요청. courseId={}, countryId={}",
                courseId, countryId);

        validateFilters(courseId, countryId);

        List<CouponPolicy> couponPolicies = courseId == null
                ? couponPolicyRepository.findAll()
                : couponPolicyRepository.findByCourseId(courseId);

        LocalDateTime now = LocalDateTime.now();
        Map<Long, LmsCoursePort.CourseSummary> courseSummaries = lmsCoursePort.findCourseSummaries(
                couponPolicies.stream()
                        .map(CouponPolicy::getCourseId)
                        .distinct()
                        .toList()
        );
        Map<Long, UserCouponRepository.CouponUsageCount> usageCounts = userCouponRepository.countByCouponPolicyIds(
                couponPolicies.stream()
                        .map(CouponPolicy::getId)
                        .distinct()
                        .toList(),
                now
        );
        List<CouponPolicyStatisticsResult> policyStatistics = new ArrayList<>();

        for (CouponPolicy couponPolicy : couponPolicies) {
            LmsCoursePort.CourseSummary courseSummary = courseSummaries.get(couponPolicy.getCourseId());

            if (courseSummary == null) {
                continue;
            }

            if (courseId != null && !courseSummary.courseId().equals(courseId)) {
                continue;
            }

            if (countryId != null && !courseSummary.countryId().equals(countryId)) {
                continue;
            }

            UserCouponRepository.CouponUsageCount usageCount = usageCounts.getOrDefault(
                    couponPolicy.getId(),
                    new UserCouponRepository.CouponUsageCount(couponPolicy.getId(), 0, 0, 0, 0)
            );

            policyStatistics.add(new CouponPolicyStatisticsResult(
                    couponPolicy.getId(),
                    couponPolicy.getCouponName(),
                    couponPolicy.getDiscountType(),
                    couponPolicy.getDiscountValue(),
                    courseSummary.courseId(),
                    courseSummary.courseTitle(),
                    courseSummary.countryId(),
                    courseSummary.countryName(),
                    Math.toIntExact(usageCount.issuedCount()),
                    Math.toIntExact(usageCount.usedCount()),
                    Math.toIntExact(usageCount.expiredCount()),
                    Math.toIntExact(usageCount.availableCount())
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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issueWelcomeCoupon(IssueWelcomeCouponCommand command) {
        if (command.userId() == null) {
            return;
        }

        if (userCouponRepository.existsByUserIdAndCouponName(command.userId(), WELCOME_COUPON_NAME)) {
            log.info("[WelcomeCoupon] Welcome coupon already issued. userId={}", command.userId());
            return;
        }

        try {
            UserCoupon savedCoupon = userCouponRepository.save(UserCoupon.issueWelcome(command.userId()));

            log.info(
                    "[WelcomeCoupon] Issued welcome coupon. userId={}, userCouponId={}",
                    command.userId(),
                    savedCoupon.getId()
            );
        } catch (DataIntegrityViolationException exception) {
            if (userCouponRepository.existsByUserIdAndCouponName(command.userId(), WELCOME_COUPON_NAME)) {
                log.info("[WelcomeCoupon] Welcome coupon already issued by concurrent request. userId={}",
                        command.userId());
                return;
            }

            throw exception;
        }
    }

    private void validateCourse(Long courseId) {
        lmsCoursePort.validateCourseExists(courseId);
    }

    private void validateCouponNameNotDuplicated(
            Long courseId,
            String couponName
    ) {
        if (couponPolicyRepository.existsByCourseIdAndCouponName(courseId, couponName)) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 이미 사용 중인 쿠폰명입니다. courseId={}, couponName={}",
                    courseId, couponName);
            throw new BenefitException(BenefitErrorCode.DUPLICATED_COUPON_POLICY_NAME);
        }
    }

    private void validateCouponNameNotDuplicated(
            Long courseId,
            String couponName,
            Long couponPolicyId
    ) {
        if (couponPolicyRepository.existsByCourseIdAndCouponNameAndIdNot(courseId, couponName, couponPolicyId)) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 이미 사용 중인 쿠폰명입니다. courseId={}, couponPolicyId={}, couponName={}",
                    courseId, couponPolicyId, couponName);
            throw new BenefitException(BenefitErrorCode.DUPLICATED_COUPON_POLICY_NAME);
        }
    }

    private void validateCouponPolicy(
            String discountType,
            int discountValue
    ) {
        if (discountType == null || discountType.isBlank()) {
            throw new BenefitException(BenefitErrorCode.INVALID_COUPON_POLICY);
        }

        String normalizedDiscountType = discountType.toUpperCase();

        if (!normalizedDiscountType.equals("RATE") && !normalizedDiscountType.equals("AMOUNT")) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 지원하지 않는 할인 타입입니다. discountType={}",
                    discountType);
            throw new BenefitException(BenefitErrorCode.INVALID_COUPON_POLICY);
        }

        if (discountValue <= 0) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 할인 값은 0보다 커야 합니다. discountValue={}",
                    discountValue);
            throw new BenefitException(BenefitErrorCode.INVALID_COUPON_POLICY);
        }

        if (normalizedDiscountType.equals("RATE") && discountValue > 100) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 할인율은 100을 초과할 수 없습니다. discountValue={}",
                    discountValue);
            throw new BenefitException(BenefitErrorCode.INVALID_COUPON_POLICY);
        }

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

}
