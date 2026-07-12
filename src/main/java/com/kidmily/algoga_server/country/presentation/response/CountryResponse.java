package com.kidmily.algoga_server.country.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "국가 응답")
public record CountryResponse(
        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "국가 코드", example = "JP")
        String countryCode,

        @Schema(description = "국가명", example = "일본")
        String countryName,

        @Schema(description = "대륙 코드", example = "ASIA")
        String continentCode,

        @Schema(description = "대륙명", example = "아시아")
        String continentName,

        @Schema(description = "활성 여부", example = "true")
        boolean active,

        @Schema(description = "강의 수", example = "5")
        long courseCount
) {
}
