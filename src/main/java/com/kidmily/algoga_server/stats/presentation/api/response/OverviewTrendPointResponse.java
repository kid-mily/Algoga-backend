package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "돈 요약 추이 한 점 (단위=HOUR/DAY/MONTH). 그래프의 총매출·환불·순매출 선용")
public record OverviewTrendPointResponse(
        @Schema(description = "구간 라벨 (HOUR='14:00' / DAY='2026-07-14' / MONTH='2026-07')", example = "2026-07-14")
        String label,

        @Schema(description = "총매출", example = "8800000")
        long totalRevenue,

        @Schema(description = "환불액", example = "1400000")
        long refund,

        @Schema(description = "순매출(총매출-환불)", example = "7400000")
        long netRevenue
) {}
