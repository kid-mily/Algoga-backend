package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.lms.domain.model.CouponPolicy;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseReward;
import com.kidmily.algoga_server.lms.domain.model.MileageHistory;
import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;
import com.kidmily.algoga_server.lms.domain.model.UserCoupon;
import com.kidmily.algoga_server.lms.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRewardRepository;
import com.kidmily.algoga_server.lms.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseRewardService implements CourseRewardUseCase {

    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final CourseRewardRepository courseRewardRepository;

    @Override
    public CourseReward rewardCourse(RewardCourseCommand command) {
        return rewardCourseWithDetails(command).courseReward();
    }

    public CourseRewardResult rewardCourseWithDetails(RewardCourseCommand command) {
        log.info("[Course Reward Command] 강의 보상 지급 요청. userId={}, courseId={}",
                command.userId(), command.courseId());

        Course course = courseRepository.findByIdAndDeletedFalse(command.courseId())
                .orElseThrow(() -> {
                    log.warn("[Course Reward Command] 보상 지급 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                            command.courseId());
                    return new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
                });

        validateCourseCompleted(command.userId(), command.courseId());
        validateRewardNotGranted(command.userId(), command.courseId());

        QuizSubmission quizSubmission = quizSubmissionRepository.findByUserIdAndCourseId(
                command.userId(),
                command.courseId()
        ).orElseThrow(() -> {
            log.warn("[Course Reward Command] 보상 지급 실패. 퀴즈 제출 내역이 없습니다. userId={}, courseId={}",
                    command.userId(), command.courseId());
            return new LmsException(LmsErrorCode.QUIZ_NOT_SUBMITTED);
        });

        List<CouponPolicy> couponPolicies = couponPolicyRepository.findActiveByCourseId(command.courseId());

        if (couponPolicies.isEmpty()) {
            log.warn("[Course Reward Command] 보상 지급 실패. 등록된 활성 쿠폰 정책이 없습니다. courseId={}",
                    command.courseId());
            throw new LmsException(LmsErrorCode.COUPON_POLICY_NOT_FOUND);
        }

        List<UserCoupon> issuedCoupons = issueCoupons(command.userId(), couponPolicies);

        int mileageRate = calculateMileageRate(quizSubmission.getCorrectCount());
        int mileageAmount = calculateMileageAmount(course.getPrice(), mileageRate);

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

        return new CourseRewardResult(
                savedCourseReward,
                issuedCoupons,
                savedMileageHistory,
                mileageRate
        );
    }

    private void validateCourseCompleted(
            Long userId,
            Long courseId
    ) {
        if (!courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Reward Command] 보상 지급 실패. 강의 이수 내역이 없습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.COURSE_COMPLETION_NOT_FOUND);
        }
    }

    private void validateRewardNotGranted(
            Long userId,
            Long courseId
    ) {
        if (courseRewardRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[Course Reward Command] 보상 지급 실패. 이미 보상이 지급되었습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.COURSE_REWARD_ALREADY_GRANTED);
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

    public record CourseRewardResult(
            CourseReward courseReward,
            List<UserCoupon> issuedCoupons,
            MileageHistory mileageHistory,
            int mileageRate
    ) {
    }
}