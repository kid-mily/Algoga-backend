package com.kidmily.algoga_server.country.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "국가 응답")
public record CountryResponse(

        @Schema(description = "국가 ID")
        Long countryId,

        @Schema(description = "대륙")
        String contient,

        @Schema(description = "국가명")
        String name
) {
}
