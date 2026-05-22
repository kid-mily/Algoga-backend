package com.kidmily.algoga_server.notice.presentation.api.request;

import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공지사항 등록 요청 DTO")
public record CreateNoticeRequest(
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        @Schema(description = "공지사항 제목", example = "V1.2 업데이트 안내")
        String title,

        @NotBlank(message = "공지사항 본문 내용은 필수입니다.")
        @Schema(description = "공지사항 본문", example = "새로운 기능이 추가되었습니다...")
        String content,

        // 🔥 필드명을 noticeTagType -> type 으로 변경
        @NotNull(message = "공지사항 태그는 필수입니다.")
        @Schema(description = "공지사항 태그 타입", example = "NOTICE")
        NoticeTagType type
) {
}