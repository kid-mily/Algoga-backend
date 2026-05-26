package com.kidmily.algoga_server.banner.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 메인 화면 조회 응답 DTO")
public record BannerResponse(
        @Schema(description = "배너 ID", example = "1")
        Long bannerId,

        @Schema(description = "배너 이미지/영상 URL", example = "https://algoga-bucket.kro.kr/.../abc.mp4")
        String imageUrl,

        // 🌟 프론트엔드 렌더링을 위한 파일 타입 추가
        @Schema(description = "파일 타입 (IMAGE, VIDEO 등)", example = "VIDEO")
        String fileType,

        @Schema(description = "배너 클릭 시 이동할 링크 URL", example = "https://algoga.com/event")
        String linkUrl,

        @Schema(description = "배너 대체 텍스트 또는 설명", example = "여름 맞이 특가 이벤트 배너")
        String text
) {
}