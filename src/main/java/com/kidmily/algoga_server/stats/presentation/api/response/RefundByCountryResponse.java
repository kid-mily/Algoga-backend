package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나라별 환불 (예약건수·환불율·위험도 평가 포함)")
public record RefundByCountryResponse(
        Long countryId,
        String countryName,
        @Schema(description = "예약 건수(기간 내 생성)") long bookingCount,
        @Schema(description = "환불 건수(COMPLETED)") long refundCount,
        @Schema(description = "환불액") long refundAmount,
        @Schema(description = "환불율(%) = 환불액 / 나라 예약결제 매출 × 100") double refundRate,
        @Schema(description = "위험도 평가: 양호(≤10%) / 주의(≤20%) / 위험(>20%)") String grade
) {
    public static RefundByCountryResponse of(Long countryId, String countryName,
                                             long bookingCount, long refundCount,
                                             long refundAmount, long bookingRevenue) {
        double rate = bookingRevenue == 0 ? 0.0
                : Math.round((double) refundAmount / bookingRevenue * 10000.0) / 100.0;
        String grade = rate <= 10.0 ? "양호" : (rate <= 20.0 ? "주의" : "위험");
        return new RefundByCountryResponse(countryId, countryName, bookingCount,
                refundCount, refundAmount, rate, grade);
    }
}
