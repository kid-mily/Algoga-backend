package com.kidmily.algoga_server.benefit.application.result;

import com.kidmily.algoga_server.benefit.domain.model.CourseReward;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;

import java.time.LocalDateTime;
import java.util.List;

public record CourseRewardResult(
        Long rewardId,
        Long userId,
        Long courseId,
        int issuedCouponCount,
        List<IssuedCouponResult> issuedCoupons,
        Long mileageHistoryId,
        int mileageRate,
        int mileageAmount,
        LocalDateTime rewardedAt
) {
    public static CourseRewardResult from(
            CourseReward courseReward,
            List<UserCoupon> issuedCoupons,
            MileageHistory mileageHistory,
            int mileageRate
    ) {
        return new CourseRewardResult(
                courseReward.getId(),
                courseReward.getUserId(),
                courseReward.getCourseId(),
                courseReward.getIssuedCouponCount(),
                issuedCoupons.stream().map(IssuedCouponResult::from).toList(),
                mileageHistory.getId(),
                mileageRate,
                mileageHistory.getAmount(),
                courseReward.getRewardedAt()
        );
    }
}