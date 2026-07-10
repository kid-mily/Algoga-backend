package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유입 경로별 전환 요약")
public record InflowSummaryResponse(

        @Schema(description = "전체 가입자 수(기간 내 가입)", example = "5460")
        long totalSignups,

        @Schema(description = "전체 순매출(기간 내 성공결제 - 환불)", example = "726000000")
        long totalNetRevenue,

        @Schema(description = "최고 효율 경로(ARPU 최고)", example = "추천")
        String topChannel,

        @Schema(description = "최고 효율 경로의 1인당 매출(ARPU)", example = "158065")
        long topChannelArpu
) {}
