package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "가입월 코호트 분석 응답 (히트맵 + 누적매출)")
public record CohortResponse(

        @Schema(description = "가입 후 추적 개월 수(열 개수). M0(가입한 달) ~ M(maxMonths-1)", example = "12")
        int maxMonths,

        @Schema(description = "가입월 코호트 목록 (가입월 오름차순)")
        List<CohortRow> cohorts
) {
    @Schema(description = "가입월 코호트 한 행")
    public record CohortRow(

            @Schema(description = "가입월 (YYYY-MM)", example = "2026-01")
            String cohortMonth,

            @Schema(description = "해당 월 가입자 수(활성)", example = "120")
            int cohortSize,

            @Schema(description = "히트맵 값: M0~M(max-1) 각 시점까지 1건 이상 결제한 누적 유저 비율(%). "
                    + "아직 도래하지 않은 미래 시점은 null(빈 칸)", example = "[0.0, 8.3, 15.0]")
            List<Double> retentionRate,

            @Schema(description = "누적매출: M0~M(max-1) 각 시점까지 해당 코호트의 누적 결제액(원). 미래 시점은 null",
                    example = "[0, 1200000, 3400000]")
            List<Long> cumulativeRevenue
    ) {}
}
