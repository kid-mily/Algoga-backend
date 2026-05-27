package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.CourseReward;
import com.kidmily.algoga_server.lms.domain.model.MileageHistory;
import com.kidmily.algoga_server.lms.domain.model.UserCoupon;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "강의 보상 지급 응답")
public record CourseRewardResponse(

        @Schema(description = "보상 ID", example = "1")
        Long rewardId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "발급된 쿠폰 수", example = "2")
        int issuedCouponCount,

        @Schema(description = "발급된 쿠폰 목록")
        List<IssuedCouponResponse> issuedCoupons,

        @Schema(description = "마일리지 내역 ID", example = "1")
        Long mileageHistoryId,

        @Schema(description = "마일리지 지급 비율", example = "10")
        int mileageRate,

        @Schema(description = "지급 마일리지", example = "10000")
        int mileageAmount,

        @Schema(description = "보상 지급 일시", example = "2026-05-26T12:30:00")
        LocalDateTime rewardedAt
) {

    public static CourseRewardResponse from(
            CourseReward courseReward,
            List<UserCoupon> issuedCoupons,
            MileageHistory mileageHistory,
            int mileageRate
    ) {
        return new CourseRewardResponse(
                courseReward.getId(),
                courseReward.getUserId(),
                courseReward.getCourseId(),
                courseReward.getIssuedCouponCount(),
                issuedCoupons.stream()
                        .map(IssuedCouponResponse::from)
                        .toList(),
                mileageHistory.getId(),
                mileageRate,
                mileageHistory.getAmount(),
                courseReward.getRewardedAt()
        );
    }

    @Schema(description = "발급 쿠폰 응답")
    public record IssuedCouponResponse(

            @Schema(description = "사용자 쿠폰 ID", example = "1")
            Long userCouponId,

            @Schema(description = "쿠폰 정책 ID", example = "1")
            Long couponPolicyId,

            @Schema(description = "쿠폰명", example = "오사카 강의 수료 할인 쿠폰")
            String couponName,

            @Schema(description = "할인 타입", example = "RATE")
            String discountType,

            @Schema(description = "할인 값", example = "10")
            int discountValue,

            @Schema(description = "쿠폰 상태", example = "ISSUED")
            String status,

            @Schema(description = "발급일시", example = "2026-05-26T12:30:00")
            LocalDateTime issuedAt,

            @Schema(description = "만료일시", example = "2026-06-25T12:30:00")
            LocalDateTime expiredAt
    ) {

        public static IssuedCouponResponse from(UserCoupon userCoupon) {
            return new IssuedCouponResponse(
                    userCoupon.getId(),
                    userCoupon.getCouponPolicyId(),
                    userCoupon.getCouponName(),
                    userCoupon.getDiscountType(),
                    userCoupon.getDiscountValue(),
                    userCoupon.getStatus(),
                    userCoupon.getIssuedAt(),
                    userCoupon.getExpiredAt()
            );
        }
    }
}