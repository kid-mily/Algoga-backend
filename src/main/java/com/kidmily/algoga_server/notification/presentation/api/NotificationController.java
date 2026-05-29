package com.kidmily.algoga_server.notification.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.notification.application.usecase.NotificationCommandUseCase;
import com.kidmily.algoga_server.notification.application.usecase.NotificationQueryUseCase;
import com.kidmily.algoga_server.notification.exception.NotificationErrorCode;
import com.kidmily.algoga_server.notification.exception.NotificationException;
import com.kidmily.algoga_server.notification.presentation.api.response.NotificationListResponse;
import com.kidmily.algoga_server.notification.presentation.api.response.UnreadCountResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;
    private final NotificationCommandUseCase notificationCommandUseCase;


    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_UNAUTHORIZED);
        }
        return ((CustomUserDetails) principal).getUser().getId();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "종 아이콘 뱃지에 표시할 읽지 않은 알림 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 비로그인이면 0 반환
        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity.ok(ApiResponse.success(
                    "UNREAD_COUNT_FOUND",
                    "읽지 않은 알림 개수 조회에 성공했습니다.",
                    new UnreadCountResponse(0)
            ));
        }

        Long currentUserId = ((CustomUserDetails) principal).getUser().getId();
        long count = notificationQueryUseCase.getUnreadCount(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(
                "UNREAD_COUNT_FOUND",
                "읽지 않은 알림 개수 조회에 성공했습니다.",
                new UnreadCountResponse(count)
        ));
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 리스트를 최신순으로 조회합니다.")
    @ApiErrorCodeExample(domain = NotificationErrorCode.class, value = {
            "NOTIFICATION_UNAUTHORIZED"
    })
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "8")
            @RequestParam(defaultValue = "8") int size,

            @Parameter(description = "읽음 여부 필터 (전체: 생략, 읽지 않음: false)")
            @RequestParam(required = false) Boolean isRead
    ) {
        Long currentUserId = getCurrentUserId();

        NotificationListResponse responseData = notificationQueryUseCase.getNotifications(
                currentUserId, isRead, page, size);

        return ResponseEntity.ok(ApiResponse.success(
                "NOTIFICATIONS_FOUND",
                "알림 목록 조회에 성공했습니다.",
                responseData
        ));
    }

    // 개별 읽음
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 개별 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @ApiErrorCodeExample(domain = NotificationErrorCode.class, value = {
            "NOTIFICATION_UNAUTHORIZED",
            "NOTIFICATION_NOT_FOUND"
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable Long notificationId
    ) {
        Long currentUserId = getCurrentUserId();
        notificationCommandUseCase.markAsRead(currentUserId, notificationId);
        return ResponseEntity.noContent().build();
    }

    // 전체 읽음
    @PatchMapping("/read-all")
    @Operation(summary = "알림 전체 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @ApiErrorCodeExample(domain = NotificationErrorCode.class, value = {
            "NOTIFICATION_UNAUTHORIZED"
    })
    public ResponseEntity<Void> markAllAsRead() {
        Long currentUserId = getCurrentUserId();
        notificationCommandUseCase.markAllAsRead(currentUserId);
        return ResponseEntity.noContent().build();
    }

    // 개별 삭제
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 개별 삭제", description = "특정 알림을 삭제합니다.")
    @ApiErrorCodeExample(domain = NotificationErrorCode.class, value = {
            "NOTIFICATION_UNAUTHORIZED",
            "NOTIFICATION_NOT_FOUND"
    })
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable Long notificationId
    ) {
        Long currentUserId = getCurrentUserId();
        notificationCommandUseCase.deleteNotification(currentUserId, notificationId);
        return ResponseEntity.noContent().build();
    }

    // 전체 삭제
    @DeleteMapping
    @Operation(summary = "알림 전체 삭제", description = "모든 알림을 삭제합니다.")
    @ApiErrorCodeExample(domain = NotificationErrorCode.class, value = {
            "NOTIFICATION_UNAUTHORIZED"
    })
    public ResponseEntity<Void> deleteAllNotifications() {
        Long currentUserId = getCurrentUserId();
        notificationCommandUseCase.deleteAllNotifications(currentUserId);
        return ResponseEntity.noContent().build();
    }
}