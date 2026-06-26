package com.kidmily.algoga_server.notice.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo; // 🌟 추가
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class") // 🌟 추가
@Schema(description = "메인 화면 공지사항 목록 응답 DTO")
public record NoticeMainResponse(
        @Schema(description = "공지사항 ID", example = "1")
        Long noticeId,

        @Schema(description = "공지사항 태그", example = "EVENT")
        NoticeTagType tag,

        @Schema(description = "공지사항 제목", example = "여름 맞이 이벤트 안내")
        String title,

        @Schema(description = "작성 날짜", example = "2026-05-22")
        String date,

        @Schema(description = "작성 시간", example = "15:30")
        String time
) {
}