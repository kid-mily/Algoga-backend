package com.kidmily.algoga_server.notification.presentation.api.response;

import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 수신 설정 응답")
public record NotificationSettingResponse(

        @Schema(description = "커뮤니티 알림 수신 여부", example = "true")
        Boolean communityEnabled
) {
    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(setting.getCommunityEnabled());
    }
}