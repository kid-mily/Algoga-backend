package com.kidmily.algoga_server.notification.presentation.api.response;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 항목")
public record NotificationItemResponse(
        @Schema(description = "알림 ID", example = "1")
        Long notificationId,

        @Schema(description = "알림 타입", example = "POST_COMMENTED")
        NotificationType type,

        @Schema(description = "알림 메시지", example = "김민준님이 내 게시글에 댓글을 달았습니다")
        String message,

        @Schema(description = "읽음 여부", example = "false")
        Boolean isRead,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt
) {}