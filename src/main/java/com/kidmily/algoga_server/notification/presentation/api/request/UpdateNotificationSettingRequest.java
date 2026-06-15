package com.kidmily.algoga_server.notification.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 수신 설정 변경 요청")
public record UpdateNotificationSettingRequest(


        @Schema(description = "커뮤니티 알림 수신 여부", example = "true")
        @NotNull(message = "커뮤니티 알림 설정 값을 입력해주세요.")
        Boolean communityEnabled
) {}