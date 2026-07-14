package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의별 수강자·수료율 (관심도, 수강자 내림차순 순위)")
public record InterestLectureResponse(

        @Schema(description = "순위(수강자 수 기준)", example = "1")
        int rank,

        @Schema(description = "강의명", example = "일본 여행 완벽 준비 가이드")
        String lectureTitle,

        @Schema(description = "나라명", example = "일본")
        String country,

        @Schema(description = "수강자 수", example = "748")
        long enrollCount,

        @Schema(description = "평균 진도율(%)", example = "72")
        int averageProgressRate,

        @Schema(description = "수료율(%)", example = "71.0")
        double completionRate,

        @Schema(description = "수료율 상태 (NORMAL: 정상, WARNING: 주의, RISK: 위험)", example = "NORMAL")
        String completionStatus
) {}
