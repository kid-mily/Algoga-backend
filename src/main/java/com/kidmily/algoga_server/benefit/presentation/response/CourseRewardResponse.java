package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.CourseRewardResult;
import com.kidmily.algoga_server.benefit.application.result.IssuedCouponResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "강의 수료 보상 지급 응답")
public record CourseRewardResponse(
        @Schema(description = "보상 지급 ID", example = "1")
        Long rewardId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "발급된 쿠폰 수", example = "1")
        int issuedCouponCount,

        @Schema(description = "발급된 쿠폰 목록")
        List<IssuedCouponResponse> issuedCoupons,

        @Schema(description = "마일리지 내역 ID", example = "10")
        Long mileageHistoryId,

        @Schema(description = "마일리지 적립률. 퍼센트 단위", example = "10")
        int mileageRate,

        @Schema(description = "적립 마일리지 금액", example = "1500")
        int mileageAmount,

        @Schema(description = "보상 지급 일시", example = "2026-06-11T17:30:00")
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

    @Schema(description = "발급 쿠폰 응답")
    public record IssuedCouponResponse(
            @Schema(description = "사용자 쿠폰 ID", example = "1")
            Long userCouponId,

            @Schema(description = "쿠폰 정책 ID", example = "12")
            Long couponPolicyId,

            @Schema(description = "쿠폰명", example = "강의 수료 할인 쿠폰")
            String couponName,

            @Schema(description = "할인 타입. RATE 또는 AMOUNT", example = "RATE")
            String discountType,

            @Schema(description = "할인 값", example = "10")
            int discountValue,

            @Schema(description = "쿠폰 상태. ISSUED, USED, EXPIRED", example = "ISSUED")
            String status,

            @Schema(description = "쿠폰 발급 일시", example = "2026-06-11T17:30:00")
            LocalDateTime issuedAt,

            @Schema(description = "쿠폰 만료 일시", example = "2026-07-11T17:30:00")
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
