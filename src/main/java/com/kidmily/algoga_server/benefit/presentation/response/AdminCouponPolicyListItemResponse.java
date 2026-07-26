package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.AdminCouponPolicyListItemResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 통합 쿠폰 정책 목록 응답")
public record AdminCouponPolicyListItemResponse(
        @Schema(description = "쿠폰 정책 ID", example = "12")
        Long couponPolicyId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "쿠폰을 등록한 관리자 ID", example = "3")
        Long managerId,

        @Schema(description = "쿠폰명", example = "웰컴쿠폰")
        String couponName,

        @Schema(description = "할인 타입", example = "RATE")
        String discountType,

        @Schema(description = "할인 값", example = "10")
        int discountValue,

        @Schema(description = "유효 기간 일수", example = "30")
        int validDays,

        @Schema(description = "활성 여부", example = "true")
        boolean active,

        @Schema(description = "생성일시", example = "2026-06-11T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2026-06-11T10:30:00")
        LocalDateTime updatedAt
) {
    public static AdminCouponPolicyListItemResponse from(AdminCouponPolicyListItemResult couponPolicy) {
        return new AdminCouponPolicyListItemResponse(
                couponPolicy.couponPolicyId(),
                couponPolicy.courseId(),
                couponPolicy.courseTitle(),
                couponPolicy.managerId(),
                couponPolicy.couponName(),
                couponPolicy.discountType(),
                couponPolicy.discountValue(),
                couponPolicy.validDays(),
                couponPolicy.active(),
                couponPolicy.createdAt(),
                couponPolicy.updatedAt()
        );
    }
}
