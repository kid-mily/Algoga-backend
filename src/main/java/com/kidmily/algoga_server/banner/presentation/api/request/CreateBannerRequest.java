package com.kidmily.algoga_server.banner.presentation.api.request;

import com.kidmily.algoga_server.banner.presentation.api.validation.ValidBannerImage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "배너 등록 요청 DTO (multipart/form-data)")
public record CreateBannerRequest(
        @NotNull(message = "배너 이미지는 필수입니다.")
        @ValidBannerImage
        @Schema(description = "업로드할 배너 이미지 파일")
        MultipartFile image,

        @Schema(description = "배너 클릭 시 이동할 링크 URL", example = "https://algoga.com/event")
        String linkUrl,

        @Schema(description = "배너 대체 텍스트 또는 설명", example = "여름 맞이 특가 이벤트 배너")
        String text,

        @NotNull(message = "공개 상태 설정은 필수입니다.")
        @Schema(description = "배너 공개 여부 (true: 공개, false: 비공개)", example = "true")
        Boolean isVisible
) {
}