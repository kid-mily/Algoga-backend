package com.kidmily.algoga_server.notification.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽지 않은 알림 개수 응답")
public record UnreadCountResponse(
        @Schema(description = "읽지 않은 알림 개수", example = "2")
        long count
) {}