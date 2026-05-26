package com.kidmily.algoga_server.banner.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "배너 등록 요청 DTO (multipart/form-data)")
public record CreateBannerRequest(
        @NotNull(message = "배너 이미지는 필수입니다.")
        @Schema(description = "업로드할 배너 이미지 파일")
        MultipartFile image,

        @Schema(description = "배너 클릭 시 이동할 링크 URL", example = "https://algoga.com/event")
        String linkUrl,

        @Schema(description = "배너 대체 텍스트 또는 설명", example = "여름 맞이 특가 이벤트 배너")
        String text,

        // CreateBannerRequest.java 와 UpdateBannerRequest.java 의 날짜 필드 수정

        @NotBlank(message = "시작일은 필수입니다. (예: 2026-06-01)")
        @Schema(description = "배너 노출 시작일 (YYYY-MM-DD 포맷)", example = "2026-06-01")
        String startDate,

        @NotBlank(message = "종료일은 필수입니다. (예: 2026-06-30)")
        @Schema(description = "배너 노출 종료일 (YYYY-MM-DD 포맷)", example = "2026-06-30")
        String endDate,

        // 🔥 누락되었던 isVisible 추가
        @NotNull(message = "공개 상태 설정은 필수입니다.")
        @Schema(description = "배너 공개 여부 (true: 공개, false: 비공개)", example = "true")
        Boolean isVisible
) {
}