package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.CourseRewardResult;
import com.kidmily.algoga_server.benefit.application.result.IssuedCouponResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Course reward response")
public record CourseRewardResponse(
        Long rewardId,
        Long userId,
        Long courseId,
        int issuedCouponCount,
        List<IssuedCouponResponse> issuedCoupons,
        Long mileageHistoryId,
        int mileageRate,
        int mileageAmount,
        LocalDateTime rewardedAt
) {
    public static CourseRewardResponse from(CourseRewardResult result) {
        return new CourseRewardResponse(
                result.rewardId(),
                result.userId(),
                result.courseId(),
                result.issuedCouponCount(),
                result.issuedCoupons().stream().map(IssuedCouponResponse::from).toList(),
                result.mileageHistoryId(),
                result.mileageRate(),
                result.mileageAmount(),
                result.rewardedAt()
        );
    }

    @Schema(description = "Issued coupon response")
    public record IssuedCouponResponse(
            Long userCouponId,
            Long couponPolicyId,
            String couponName,
            String discountType,
            int discountValue,
            String status,
            LocalDateTime issuedAt,
            LocalDateTime expiredAt
    ) {
        public static IssuedCouponResponse from(IssuedCouponResult userCoupon) {
            return new IssuedCouponResponse(
                    userCoupon.userCouponId(),
                    userCoupon.couponPolicyId(),
                    userCoupon.couponName(),
                    userCoupon.discountType(),
                    userCoupon.discountValue(),
                    userCoupon.status(),
                    userCoupon.issuedAt(),
                    userCoupon.expiredAt()
            );
        }
    }
}