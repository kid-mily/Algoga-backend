package com.kidmily.algoga_server.benefit.presentation.response;

import com.kidmily.algoga_server.benefit.application.result.CouponPolicyStatisticsResult;
import com.kidmily.algoga_server.benefit.application.result.CouponStatisticsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "쿠폰 통계 응답")
public record CouponStatisticsResponse(

        @Schema(description = "필터 강의 ID", example = "3")
        Long filterCourseId,

        @Schema(description = "필터 국가 ID", example = "1")
        Long filterCountryId,

        @Schema(description = "총 쿠폰 정책 수", example = "2")
        int totalPolicyCount,

        @Schema(description = "총 발급 쿠폰 수", example = "15")
        int totalIssuedCouponCount,

        @Schema(description = "총 사용 쿠폰 수", example = "3")
        int totalUsedCouponCount,

        @Schema(description = "총 만료 쿠폰 수", example = "1")
        int totalExpiredCouponCount,

        @Schema(description = "총 사용 가능 쿠폰 수", example = "11")
        int totalAvailableCouponCount,

        @Schema(description = "쿠폰별 통계 목록")
        List<CouponPolicyStatisticsResponse> policies
) {

    public static CouponStatisticsResponse from(CouponStatisticsResult result) {
        return new CouponStatisticsResponse(
                result.filterCourseId(),
                result.filterCountryId(),
                result.totalPolicyCount(),
                result.totalIssuedCouponCount(),
                result.totalUsedCouponCount(),
                result.totalExpiredCouponCount(),
                result.totalAvailableCouponCount(),
                result.policies()
                        .stream()
                        .map(CouponPolicyStatisticsResponse::from)
                        .toList()
        );
    }

    @Schema(description = "쿠폰 정책별 통계")
    public record CouponPolicyStatisticsResponse(

            @Schema(description = "쿠폰 정책 ID", example = "1")
            Long couponPolicyId,

            @Schema(description = "쿠폰명", example = "오사카 강의 수료 할인 쿠폰")
            String couponName,

            @Schema(description = "할인 타입", example = "RATE")
            String discountType,

            @Schema(description = "할인 값", example = "10")
            int discountValue,

            @Schema(description = "강의 ID", example = "3")
            Long courseId,

            @Schema(description = "강의명", example = "오사카 여행 준비 마스터")
            String courseTitle,

            @Schema(description = "국가 ID", example = "1")
            Long countryId,

            @Schema(description = "국가명", example = "일본")
            String countryName,

            @Schema(description = "발급 수", example = "10")
            int issuedCount,

            @Schema(description = "사용 수", example = "2")
            int usedCount,

            @Schema(description = "만료 수", example = "1")
            int expiredCount,

            @Schema(description = "사용 가능 수", example = "7")
            int availableCount
    ) {

        public static CouponPolicyStatisticsResponse from(CouponPolicyStatisticsResult result) {
            return new CouponPolicyStatisticsResponse(
                    result.couponPolicyId(),
                    result.couponName(),
                    result.discountType(),
                    result.discountValue(),
                    result.courseId(),
                    result.courseTitle(),
                    result.countryId(),
                    result.countryName(),
                    result.issuedCount(),
                    result.usedCount(),
                    result.expiredCount(),
                    result.availableCount()
            );
        }
    }
}