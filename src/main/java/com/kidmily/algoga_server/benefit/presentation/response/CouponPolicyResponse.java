package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의별 쿠폰 정책 응답")
public record CouponPolicyResponse(

        @Schema(description = "쿠폰 정책 ID", example = "1")
        Long couponPolicyId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "콘텐츠 매니저 ID", example = "1")
        Long managerId,

        @Schema(description = "쿠폰명", example = "오사카 강의 수료 할인 쿠폰")
        String couponName,

        @Schema(description = "할인 타입", example = "RATE")
        String discountType,

        @Schema(description = "할인 값", example = "10")
        int discountValue,

        @Schema(description = "유효기간 일수", example = "30")
        int validDays,

        @Schema(description = "활성 여부", example = "true")
        boolean active,

        @Schema(description = "생성일시", example = "2026-05-26T11:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2026-05-26T11:30:00")
        LocalDateTime updatedAt
) {

    public static CouponPolicyResponse from(CouponPolicy couponPolicy) {
        return new CouponPolicyResponse(
                couponPolicy.getId(),
                couponPolicy.getCourseId(),
                couponPolicy.getManagerId(),
                couponPolicy.getCouponName(),
                couponPolicy.getDiscountType(),
                couponPolicy.getDiscountValue(),
                couponPolicy.getValidDays(),
                couponPolicy.isActive(),
                couponPolicy.getCreatedAt(),
                couponPolicy.getUpdatedAt()
        );
    }
}