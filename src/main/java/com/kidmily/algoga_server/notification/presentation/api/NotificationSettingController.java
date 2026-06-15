package com.kidmily.algoga_server.notification.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.notification.application.command.UpdateNotificationSettingCommand;
import com.kidmily.algoga_server.notification.application.usecase.NotificationSettingCommandUseCase;
import com.kidmily.algoga_server.notification.application.usecase.NotificationSettingQueryUseCase;
import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import com.kidmily.algoga_server.notification.exception.NotificationErrorCode;
import com.kidmily.algoga_server.notification.presentation.api.request.UpdateNotificationSettingRequest;
import com.kidmily.algoga_server.notification.presentation.api.response.NotificationSettingResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/notifications/settings")
@Tag(name = "Notification Setting", description = "알림 설정 API")
public class NotificationSettingController {

    private final NotificationSettingQueryUseCase notificationSettingQueryUseCase;
    private final NotificationSettingCommandUseCase notificationSettingCommandUseCase;

    @GetMapping
    @Operation(summary = "알림 수신 설정 조회", description = "현재 사용자의 알림 설정 값을 조회합니다.")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        NotificationSetting setting = notificationSettingQueryUseCase.getSetting(userId);

        return ResponseEntity.ok(ApiResponse.success(
                "NOTIFICATION_SETTING_FOUND",
                "알림 설정 조회에 성공했습니다.",
                NotificationSettingResponse.from(setting)
        ));
    }

    @PatchMapping
    @Operation(summary = "알림 수신 설정 변경", description = "사용자의 알림 설정을 변경합니다.")
    @ApiErrorCodeExample(domain = NotificationErrorCode.class, value = {
            "NOTIFICATION_SETTING_INVALID"
    })
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateNotificationSettingRequest request
    ) {
        Long userId = userDetails.getUser().getId();

        UpdateNotificationSettingCommand command = new UpdateNotificationSettingCommand(
                userId,
                request.communityEnabled()
        );

        NotificationSetting setting = notificationSettingCommandUseCase.updateSetting(command);

        return ResponseEntity.ok(ApiResponse.success(
                "NOTIFICATION_SETTING_UPDATED",
                "알림 설정 변경에 성공했습니다.",
                NotificationSettingResponse.from(setting)
        ));
    }
}