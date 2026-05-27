package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.result.MyCouponResult;
import com.kidmily.algoga_server.benefit.application.result.MyMileageHistoryResult;
import com.kidmily.algoga_server.benefit.application.result.MyMileageResult;
import com.kidmily.algoga_server.benefit.application.usecase.MyBenefitUseCase;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyBenefitService implements MyBenefitUseCase {

    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final LmsCoursePort lmsCoursePort;

    @Override
    public List<MyCouponResult> getMyCoupons(Long userId) {
        log.info("[My Benefit Query] 내 쿠폰함 조회 요청. userId={}", userId);

        LocalDateTime now = LocalDateTime.now();

        List<MyCouponResult> results = userCouponRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(UserCoupon::getIssuedAt).reversed())
                .map(userCoupon -> toMyCouponResult(userCoupon, now))
                .toList();

        log.info("[My Benefit Query] 내 쿠폰함 조회 완료. userId={}, count={}", userId, results.size());

        return results;
    }

    @Override
    public MyMileageResult getMyMileages(Long userId) {
        log.info("[My Benefit Query] 내 마일리지 내역 조회 요청. userId={}", userId);

        List<MileageHistory> histories = mileageHistoryRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(MileageHistory::getCreatedAt).reversed())
                .toList();

        int totalEarnedMileage = histories.stream()
                .filter(this::isEarnType)
                .mapToInt(MileageHistory::getAmount)
                .sum();

        int totalUsedMileage = histories.stream()
                .filter(this::isUseType)
                .mapToInt(MileageHistory::getAmount)
                .sum();

        int totalMileage = totalEarnedMileage - totalUsedMileage;

        List<MyMileageHistoryResult> historyResults = histories.stream()
                .map(this::toMyMileageHistoryResult)
                .toList();

        log.info("[My Benefit Query] 내 마일리지 내역 조회 완료. userId={}, totalMileage={}, count={}",
                userId, totalMileage, historyResults.size());

        return new MyMileageResult(
                totalMileage,
                totalEarnedMileage,
                totalUsedMileage,
                historyResults
        );
    }

    private MyCouponResult toMyCouponResult(
            UserCoupon userCoupon,
            LocalDateTime now
    ) {
        String courseTitle = findCourseTitle(userCoupon.getCourseId());

        String status = resolveCouponStatus(userCoupon, now);
        boolean usable = "ISSUED".equals(status);

        return new MyCouponResult(
                userCoupon.getId(),
                userCoupon.getCourseId(),
                courseTitle,
                userCoupon.getCouponPolicyId(),
                userCoupon.getCouponName(),
                userCoupon.getDiscountType(),
                userCoupon.getDiscountValue(),
                status,
                usable,
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt(),
                userCoupon.getUsedAt()
        );
    }

    private MyMileageHistoryResult toMyMileageHistoryResult(MileageHistory mileageHistory) {
        String courseTitle = findCourseTitle(mileageHistory.getCourseId());

        return new MyMileageHistoryResult(
                mileageHistory.getId(),
                mileageHistory.getCourseId(),
                courseTitle,
                mileageHistory.getAmount(),
                mileageHistory.getType(),
                mileageHistory.getReason(),
                mileageHistory.getCreatedAt()
        );
    }

    private String findCourseTitle(Long courseId) {
        if (courseId == null) {
            return null;
        }

        return lmsCoursePort.findCourseSummary(courseId)
                .map(LmsCoursePort.CourseSummary::courseTitle)
                .orElse(null);
    }

    private String resolveCouponStatus(
            UserCoupon userCoupon,
            LocalDateTime now
    ) {
        if ("USED".equalsIgnoreCase(userCoupon.getStatus())) {
            return "USED";
        }

        if ("EXPIRED".equalsIgnoreCase(userCoupon.getStatus())) {
            return "EXPIRED";
        }

        if (userCoupon.getExpiredAt() != null && userCoupon.getExpiredAt().isBefore(now)) {
            return "EXPIRED";
        }

        return "ISSUED";
    }

    private boolean isEarnType(MileageHistory mileageHistory) {
        return "EARN".equalsIgnoreCase(mileageHistory.getType());
    }

    private boolean isUseType(MileageHistory mileageHistory) {
        return "USE".equalsIgnoreCase(mileageHistory.getType())
                || "USED".equalsIgnoreCase(mileageHistory.getType());
    }
}