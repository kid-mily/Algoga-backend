package com.kidmily.algoga_server.banner.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "배너 수정 요청 DTO (multipart/form-data)")
public record UpdateBannerRequest(
        @NotNull(message = "배너 ID는 필수입니다.")
        @Schema(description = "수정할 배너 ID", example = "1")
        Long bannerId,

        @Schema(description = "변경할 배너 이미지 파일 (변경하지 않으면 전송하지 않음)")
        MultipartFile image,

        @Schema(description = "배너 클릭 시 이동할 링크 URL", example = "https://algoga.com/event")
        String linkUrl,

        @Schema(description = "배너 대체 텍스트 또는 설명", example = "(수정) 여름 특가 이벤트")
        String text,

        @NotNull(message = "공개 상태 설정은 필수입니다.")
        @Schema(description = "배너 공개 여부 (true: 공개, false: 비공개)", example = "true")
        Boolean isVisible
) {
}