package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.command.UpdateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.usecase.CouponPolicyUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
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

    private final LmsCoursePort lmsCoursePort;
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
        validateCouponNameNotDuplicated(command.courseId(), command.couponName());

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

    @Override
    @Transactional
    public CouponPolicy updateCouponPolicy(UpdateCouponPolicyCommand command) {
        log.info("[Coupon Policy Command] 쿠폰 정책 수정 요청. courseId={}, couponPolicyId={}, couponName={}",
                command.courseId(), command.couponPolicyId(), command.couponName());

        validateCourse(command.courseId());
        validateCouponPolicy(
                command.discountType(),
                command.discountValue(),
                command.validDays()
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
                command.validDays()
        ).orElseThrow(() -> {
            log.warn("[Coupon Policy Command] 쿠폰 정책 수정 실패. 존재하지 않거나 비활성화된 쿠폰 정책입니다. courseId={}, couponPolicyId={}",
                    command.courseId(), command.couponPolicyId());
            return new BenefitException(BenefitErrorCode.COUPON_POLICY_NOT_FOUND);
        });

        log.info("[Coupon Policy Command] 쿠폰 정책 수정 완료. couponPolicyId={}, courseId={}, couponName={}",
                updatedCouponPolicy.getId(),
                updatedCouponPolicy.getCourseId(),
                updatedCouponPolicy.getCouponName());

        return updatedCouponPolicy;
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
            int discountValue,
            int validDays
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

        if (validDays <= 0) {
            log.warn("[Coupon Policy] 쿠폰 정책 검증 실패. 유효기간은 1일 이상이어야 합니다. validDays={}",
                    validDays);
            throw new BenefitException(BenefitErrorCode.INVALID_COUPON_POLICY);
        }
    }
}
