package com.kidmily.algoga_server.notice.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@Schema(description = "공지사항 상세 조회 응답 DTO")
public record NoticeResponse(
        @Schema(description = "공지사항 ID", example = "1")
        Long noticeId,

        @Schema(description = "작성자(매니저) ID", example = "1")
        Long managerId,

        @Schema(description = "공지사항 태그", example = "NOTICE")
        NoticeTagType tag,

        @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "공지사항 본문", example = "내일 새벽 2시부터 4시까지 점검이 있습니다.")
        String content,

        @Schema(description = "작성 일시", example = "2026-05-22 15:30:00")
        String createdAt
) {
}