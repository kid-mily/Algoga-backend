package com.kidmily.algoga_server.lms.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대륙 응답")
public record ContinentResponse(
        @Schema(description = "대륙 코드", example = "ASIA")
        String continentCode,

        @Schema(description = "대륙명", example = "아시아")
        String continentName,

        @Schema(description = "국가 수", example = "5")
        long countryCount,

        @Schema(description = "강의 수", example = "20")
        long courseCount
) {
}
