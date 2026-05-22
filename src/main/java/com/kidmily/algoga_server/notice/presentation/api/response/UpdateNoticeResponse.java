package com.kidmily.algoga_server.notice.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 수정 응답 DTO")
public record UpdateNoticeResponse(
        @Schema(description = "수정된 공지사항 ID", example = "1")
        Long noticeId
) {
}