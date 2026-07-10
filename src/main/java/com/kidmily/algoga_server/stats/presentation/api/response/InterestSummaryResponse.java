package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나라·강의 관심도 요약 (누적 지표)")
public record InterestSummaryResponse(

        @Schema(description = "총 수강 신청 수(전 강의 합산)", example = "5069")
        long totalEnrollments,

        @Schema(description = "평균 수료율(%) = 총 수료/총 수강", example = "45.0")
        double avgCompletionRate,

        @Schema(description = "수료율 위험 강의 수(수료율 30% 미만, 수강자 1명 이상)", example = "4")
        long riskyLectureCount
) {}
