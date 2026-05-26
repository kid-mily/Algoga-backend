package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.lms.application.usecase.CouponPolicyUseCase;
import com.kidmily.algoga_server.lms.domain.model.CouponPolicy;
import com.kidmily.algoga_server.lms.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponPolicyService implements CouponPolicyUseCase {

    private final CourseRepository courseRepository;
    private final CouponPolicyRepository couponPolicyRepository;

    @Override
    @Transactional
    public CouponPolicy createCouponPolicy(CreateCouponPolicyCommand command) {
        log.info("[Coupon Policy Command] 쿠폰 정책 등록 요청. courseId={}, managerId={}, couponName={}",
                command.courseId(), command.managerId(), command.couponName());

        validateCourse(command.courseId());
        validateCouponPolicy(
                command.discountType(),
                command.discountValue(),
                command.validDays()
        );

        CouponPolicy couponPolicy = CouponPolicy.create(
                command.courseId(),
                command.managerId(),
                command.couponName(),
                command.discountType().toUpperCase(),
                command.discountValue(),
                command.validDays()
        );

        CouponPolicy savedCouponPolicy = couponPolicyRepository.save(couponPolicy);

        log.info("[Coupon Policy Command] 쿠폰 정책 등록 완료. couponPolicyId={}, courseId={}, couponName={}",
                savedCouponPolicy.getId(),
                savedCouponPolicy.getCourseId(),
                savedCouponPolicy.getCouponName());

        return savedCouponPolicy;
    }

    @Override
    public List<CouponPolicy> getCouponPolicies(Long courseId) {
        log.info("[Coupon Policy Query] 강의별 쿠폰 정책 목록 조회 요청. courseId={}", courseId);

        validateCourse(courseId);

        List<CouponPolicy> couponPolicies = couponPolicyRepository.findByCourseId(courseId);

        log.info("[Coupon Policy Query] 강의별 쿠폰 정책 목록 조회 완료. courseId={}, count={}",
                courseId, couponPolicies.size());

        return couponPolicies;
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[Coupon Policy] 쿠폰 정책 처리 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateCouponPolicy(
            String discountType,
            int discountValue,
            int validDays
    ) {
        if (discountType == null || discountType.isBlank()) {
            throw new LmsException(LmsErrorCode.INVALID_COUPON_POLICY);
        }

        String normalizedDiscountType = discountType.toUpperCase();

        if (!normalizedDiscountType.equals("RATE") && !normalizedDiscountType.equals("AMOUNT")) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 지원하지 않는 할인 타입입니다. discountType={}",
                    discountType);
            throw new LmsException(LmsErrorCode.INVALID_COUPON_POLICY);
        }

        if (discountValue <= 0) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 할인 값은 0보다 커야 합니다. discountValue={}",
                    discountValue);
            throw new LmsException(LmsErrorCode.INVALID_COUPON_POLICY);
        }

        if (normalizedDiscountType.equals("RATE") && discountValue > 100) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 할인율은 100을 초과할 수 없습니다. discountValue={}",
                    discountValue);
            throw new LmsException(LmsErrorCode.INVALID_COUPON_POLICY);
        }

        if (validDays <= 0) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 유효기간은 1일 이상이어야 합니다. validDays={}",
                    validDays);
            throw new LmsException(LmsErrorCode.INVALID_COUPON_POLICY);
        }
    }
}