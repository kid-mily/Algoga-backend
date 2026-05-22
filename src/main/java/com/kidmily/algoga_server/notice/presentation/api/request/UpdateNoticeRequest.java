package com.kidmily.algoga_server.notice.presentation.api.request;

import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공지사항 수정 요청 DTO")
public record UpdateNoticeRequest(
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        @Schema(description = "수정할 공지사항 제목", example = "(수정) V1.2 업데이트 안내")
        String title,

        @NotBlank(message = "공지사항 본문 내용은 필수입니다.")
        @Schema(description = "수정할 공지사항 본문", example = "내용이 일부 수정되었습니다.")
        String content,

        @NotNull(message = "공지사항 태그는 필수입니다.")
        @Schema(description = "수정할 공지사항 태그 타입", example = "NOTICE")
        NoticeTagType noticeTagType
) {
}