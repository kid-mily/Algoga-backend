package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.RecordCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardFailureResult;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardResult;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.model.CourseReward;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailure;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardFailureRepository;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardRepository;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CourseRewardService implements CourseRewardUseCase {

    private static final int MAX_RETRY_COUNT = 3;

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final CourseRewardRepository courseRewardRepository;
    private final CourseRewardFailureRepository courseRewardFailureRepository;
    private final LmsCoursePort lmsCoursePort;

    // 프록시를 거쳐야 rewardCourseWithDetails의 REQUIRES_NEW가 실제로 적용됨 (같은 빈 안에서의 self-invocation은 AOP를 우회하기 때문)
    // Lombok의 @RequiredArgsConstructor는 필드의 @Lazy를 생성자 파라미터로 복사하지 않아 순환참조 에러가 나므로 생성자를 직접 작성한다.
    private final CourseRewardUseCase self;

    public CourseRewardService(
            CouponPolicyRepository couponPolicyRepository,
            UserCouponRepository userCouponRepository,
            MileageHistoryRepository mileageHistoryRepository,
            CourseRewardRepository courseRewardRepository,
            CourseRewardFailureRepository courseRewardFailureRepository,
            LmsCoursePort lmsCoursePort,
            @Lazy CourseRewardUseCase self
    ) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.userCouponRepository = userCouponRepository;
        this.mileageHistoryRepository = mileageHistoryRepository;
        this.courseRewardRepository = courseRewardRepository;
        this.courseRewardFailureRepository = courseRewardFailureRepository;
        this.lmsCoursePort = lmsCoursePort;
        this.self = self;
    }

    @Override
    public CourseRewardResult rewardCourse(RewardCourseCommand command) {
        return rewardCourseWithDetails(command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

        List<UserCoupon> issuedCoupons = couponPolicies.isEmpty()
                ? List.of()
                : issueCoupons(command.userId(), couponPolicies);

        int mileageRate = calculateMileageRate(courseRewardInfo.correctCount());
        int mileageAmount = calculateMileageAmount(courseRewardInfo.maxRewardMileage(), mileageRate);

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
            return 100;
        }

        if (correctCount >= 2) {
            return 70;
        }

        return 50;
    }

    private int calculateMileageAmount(
            Integer maxRewardMileage,
            int mileageRate
    ) {
        if (maxRewardMileage == null || maxRewardMileage <= 0) {
            return 0;
        }

        return (int) Math.floor(maxRewardMileage * (mileageRate / 100.0));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(RecordCourseRewardFailureCommand command) {
        courseRewardFailureRepository.save(
                CourseRewardFailure.create(
                        command.userId(),
                        command.courseId(),
                        command.completionId(),
                        command.failureReason()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRewardFailureResult> getFailures(CourseRewardFailureStatus status) {
        return courseRewardFailureRepository.findAllByStatus(status)
                .stream()
                .map(CourseRewardFailureResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRewardFailureResult> getRetryableFailures(int maxRetryCount) {
        return courseRewardFailureRepository.findRetryableFailures(maxRetryCount)
                .stream()
                .map(CourseRewardFailureResult::from)
                .toList();
    }

    @Override
    @Transactional
    public CourseRewardFailureResult retryFailure(RetryCourseRewardFailureCommand command) {
        CourseRewardFailure failure = courseRewardFailureRepository.findById(command.failureId())
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.COURSE_REWARD_FAILURE_NOT_FOUND));

        if (failure.isResolved()) {
            throw new BenefitException(BenefitErrorCode.COURSE_REWARD_FAILURE_ALREADY_RESOLVED);
        }

        CourseRewardFailure retryingFailure = courseRewardFailureRepository.save(failure.markRetrying());

        try {
            self.rewardCourseWithDetails(
                    new RewardCourseCommand(
                            retryingFailure.getUserId(),
                            retryingFailure.getCourseId()
                    )
            );

            return CourseRewardFailureResult.from(
                    courseRewardFailureRepository.save(retryingFailure.markResolved())
            );
        } catch (Exception exception) {
            CourseRewardFailure failed = retryingFailure.markRetryFailed(
                    exception.getMessage(),
                    MAX_RETRY_COUNT
            );

            courseRewardFailureRepository.save(failed);
            throw exception;
        }
    }

}
