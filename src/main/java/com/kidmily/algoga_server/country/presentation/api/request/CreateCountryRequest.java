package com.kidmily.algoga_server.country.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "국가 등록 요청")
public record CreateCountryRequest(

        @Schema(description = "대륙", example = "아시아")
        @NotBlank(message = "대륙은 필수입니다.")
        String continent,

        @Schema(description = "국가명", example = "일본")
        @NotBlank(message = "국가명은 필수입니다.")
        String name,

        @Schema(description = "대표 공항 IATA 코드", example = "NRT")
        @NotBlank(message = "IATA 코드는 필수입니다.")
        @Size(max = 10, message = "IATA 코드는 10자 이하입니다.")
        String iataCode
) {}
