package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유입 경로별 상세 (막대/테이블용)")
public record InflowChannelResponse(

        @Schema(description = "유입 경로 (검색/광고/SNS/추천/직접/기타)", example = "추천")
        String channel,

        @Schema(description = "가입자 수", example = "620")
        long signupCount,

        @Schema(description = "순매출(성공결제 - 환불)", example = "98000000")
        long netRevenue,

        @Schema(description = "1인당 매출(ARPU) = 순매출/가입자수", example = "158065")
        long arpu
) {}
