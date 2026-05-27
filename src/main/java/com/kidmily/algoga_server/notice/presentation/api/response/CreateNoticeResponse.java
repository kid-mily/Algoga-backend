package com.kidmily.algoga_server.notice.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 등록 응답 DTO")
public record CreateNoticeResponse(
        @Schema(description = "생성된 공지사항 ID", example = "1")
        Long noticeId
) {
}