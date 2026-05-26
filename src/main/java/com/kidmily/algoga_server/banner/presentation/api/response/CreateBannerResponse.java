package com.kidmily.algoga_server.banner.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 등록 응답 DTO")
public record CreateBannerResponse(
        @Schema(description = "생성된 배너 ID", example = "1")
        Long bannerId
) {
}