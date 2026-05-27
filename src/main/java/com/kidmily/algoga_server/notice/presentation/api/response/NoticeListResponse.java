package com.kidmily.algoga_server.notice.presentation.api.response;

import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 전체 목록 응답 DTO (본문 제외)")
public record NoticeListResponse(
        @Schema(description = "공지사항 ID", example = "1")
        Long noticeId,

        @Schema(description = "공지사항 태그", example = "NOTICE")
        NoticeTagType type,

        @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "작성 날짜", example = "2026-05-22")
        String date
) {
}