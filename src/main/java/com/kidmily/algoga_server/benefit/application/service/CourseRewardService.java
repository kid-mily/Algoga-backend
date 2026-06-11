package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardResult;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.model.CourseReward;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardRepository;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
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
@Transactional
public class CourseRewardService implements CourseRewardUseCase {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final CourseRewardRepository courseRewardRepository;
    private final LmsCoursePort lmsCoursePort;

    @Override
    public CourseRewardResult rewardCourse(RewardCourseCommand command) {
        return rewardCourseWithDetails(command);
    }

    @Override
    public CourseRewardResult rewardCourseWithDetails(RewardCourseCommand command) {
        log.info("[Course Reward Command] 강의 보상 지급 요청. userId={}, courseId={}",
                command.userId(), command.courseId());

        LmsCoursePort.CourseRewardInfo courseRewardInfo = lmsCoursePort.getCourseRewardInfo(
                command.userId(),
                command.courseId()
        );

        validateRewardNotGranted(command.userId(), command.courseId());
        validateRewardPeriod(courseRewardInfo.enrolledAt());

        List<CouponPolicy> couponPolicies = couponPolicyRepository.findActiveByCourseId(command.courseId());

        if (couponPolicies.isEmpty()) {
            log.warn("[Course Reward Command] 보상 지급 실패. 등록된 활성 쿠폰 정책이 없습니다. courseId={}",
                    command.courseId());
            throw new BenefitException(BenefitErrorCode.COUPON_POLICY_NOT_FOUND);
        }

        List<UserCoupon> issuedCoupons = issueCoupons(command.userId(), couponPolicies);

        int mileageRate = calculateMileageRate(courseRewardInfo.correctCount());
        int mileageAmount = calculateMileageAmount(courseRewardInfo.coursePrice(), mileageRate);

        MileageHistory mileageHistory = MileageHistory.earn(
                command.userId(),
                command.courseId(),
                mileageAmount,
                "강의 이수 및 퀴즈 완료 보상"
        );

        MileageHistory savedMileageHistory = mileageHistoryRepository.save(mileageHistory);

        CourseReward courseReward = CourseReward.create(
                command.userId(),
                command.courseId(),
                issuedCoupons.size(),
                savedMileageHistory.getId()
        );

        CourseReward savedCourseReward = courseRewardRepository.save(courseReward);

        log.info("[Course Reward Command] 강의 보상 지급 완료. userId={}, courseId={}, rewardId={}, issuedCouponCount={}, mileageRate={}, mileageAmount={}",
                savedCourseReward.getUserId(),
                savedCourseReward.getCourseId(),
                savedCourseReward.getId(),
                savedCourseReward.getIssuedCouponCount(),
                mileageRate,
                savedMileageHistory.getAmount());

        return CourseRewardResult.from(
                savedCourseReward,
                issuedCoupons,
                savedMileageHistory,
                mileageRate
        );
    }

    private void validateRewardNotGranted(
            Long userId,
            Long courseId
    ) {
        if (courseRewardRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Reward Command] 보상 지급 실패. 이미 보상이 지급되었습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new BenefitException(BenefitErrorCode.COURSE_REWARD_ALREADY_GRANTED);
        }
    }

    private void validateRewardPeriod(LocalDateTime enrolledAt) {
        if (enrolledAt != null && LocalDateTime.now().isAfter(enrolledAt.plusMonths(1))) {
            throw new BenefitException(BenefitErrorCode.COURSE_REWARD_PERIOD_EXPIRED);
        }
    }

    private List<UserCoupon> issueCoupons(
            Long userId,
            List<CouponPolicy> couponPolicies
    ) {
        List<UserCoupon> issuedCoupons = new ArrayList<>();

        for (CouponPolicy couponPolicy : couponPolicies) {
            if (userCouponRepository.existsByUserIdAndCouponPolicyId(userId, couponPolicy.getId())) {
                continue;
            }

            UserCoupon userCoupon = UserCoupon.issue(userId, couponPolicy);
            UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);
            issuedCoupons.add(savedUserCoupon);
        }

        return issuedCoupons;
    }

    private int calculateMileageRate(int correctCount) {
        if (correctCount >= 4) {
            return 10;
        }

        if (correctCount >= 2) {
            return 7;
        }

        return 5;
    }

    private int calculateMileageAmount(
            Integer price,
            int mileageRate
    ) {
        if (price == null || price <= 0) {
            return 0;
        }

        return (int) Math.floor(price * (mileageRate / 100.0));
    }

}
