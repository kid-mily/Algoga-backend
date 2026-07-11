package com.kidmily.algoga_server.stats.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나라별 수강 수 (관심도)")
public record InterestCountryResponse(

        @Schema(description = "나라명", example = "일본")
        String country,

        @Schema(description = "수강 신청 수(그 나라 강의 합산)", example = "892")
        long enrollCount
) {}
