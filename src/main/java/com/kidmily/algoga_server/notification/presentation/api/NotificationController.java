package com.kidmily.algoga_server.notification.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.notification.application.usecase.NotificationQueryUseCase;
import com.kidmily.algoga_server.notification.presentation.api.response.UnreadCountResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "종 아이콘 뱃지에 표시할 읽지 않은 알림 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        Long currentUserId = ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUser().getId();

        long count = notificationQueryUseCase.getUnreadCount(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(
                "UNREAD_COUNT_FOUND",
                "읽지 않은 알림 개수 조회에 성공했습니다.",
                new UnreadCountResponse(count)
        ));
    }
}