package com.kidmily.algoga_server.banner.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 메인 화면 조회 응답 DTO")
public record BannerResponse(
        @Schema(description = "배너 ID", example = "1")
        Long bannerId,

        @Schema(description = "배너 이미지 URL", example = "https://algoga-bucket.kro.kr/algoga-storage/banners/abc.jpg")
        String imageUrl,

        @Schema(description = "배너 클릭 시 이동할 링크 URL", example = "https://algoga.com/event")
        String linkUrl,

        @Schema(description = "배너 대체 텍스트 또는 설명", example = "여름 맞이 특가 이벤트 배너")
        String text
) {
}