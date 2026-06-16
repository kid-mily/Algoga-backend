package com.kidmily.algoga_server.notification.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "알림 목록 응답")
public record NotificationListResponse(
        @Schema(description = "전체 읽지 않은 알림 수", example = "2")
        long unreadCount,

        @Schema(description = "읽지 않은 알림 존재 여부", example = "true")
        boolean hasUnread,

        @Schema(description = "알림 목록")
        List<NotificationItemResponse> notifications,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "전체 알림 수", example = "8")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
        int currentPage
) {}