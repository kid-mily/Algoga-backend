package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.MyCouponResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 쿠폰함 응답")
public record MyCouponResponse(

        @Schema(description = "사용자 쿠폰 ID", example = "1")
        Long userCouponId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "쿠폰 정책 ID", example = "1")
        Long couponPolicyId,

        @Schema(description = "쿠폰명", example = "오사카 강의 수료 할인 쿠폰")
        String couponName,

        @Schema(description = "할인 타입. RATE 또는 AMOUNT", example = "RATE")
        String discountType,

        @Schema(description = "할인 값", example = "10")
        int discountValue,

        @Schema(description = "쿠폰 상태. ISSUED, USED, EXPIRED", example = "ISSUED")
        String status,

        @Schema(description = "사용 가능 여부", example = "true")
        boolean usable,

        @Schema(description = "발급 일시", example = "2026-05-26T15:30:00")
        LocalDateTime issuedAt,

        @Schema(description = "만료 일시", example = "2026-06-25T15:30:00")
        LocalDateTime expiredAt,

        @Schema(description = "사용 일시", example = "2026-05-27T12:00:00")
        LocalDateTime usedAt
) {

    public static MyCouponResponse from(MyCouponResult result) {
        return new MyCouponResponse(
                result.userCouponId(),
                result.courseId(),
                result.courseTitle(),
                result.couponPolicyId(),
                result.couponName(),
                result.discountType(),
                result.discountValue(),
                result.status(),
                result.usable(),
                result.issuedAt(),
                result.expiredAt(),
                result.usedAt()
        );
    }
}