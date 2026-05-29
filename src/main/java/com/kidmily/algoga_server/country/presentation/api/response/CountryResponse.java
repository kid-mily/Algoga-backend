package com.kidmily.algoga_server.country.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "국가 응답")
public record CountryResponse(

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "대륙", example = "아시아")
        String contient,

        @Schema(description = "국가명", example = "일본")
        String name,

        @Schema(description = "대표 공항 IATA 코드", example = "NRT")
        String iataCode
) {
}