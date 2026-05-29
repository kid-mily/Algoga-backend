package com.kidmily.algoga_server.notification.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    private Long notificationId;
    private Long userId;
    private NotificationType type;
    private String message;
    private String detail;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    private Notification(Long userId, NotificationType type, String message, String detail, Long referenceId) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.detail = detail;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    private Notification(Long notificationId, Long userId, NotificationType type,
                         String message, String detail, Long referenceId, Boolean isRead, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.detail = detail;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static Notification create(Long userId, NotificationType type, String message, String detail, Long referenceId) {
        return new Notification(userId, type, message, detail, referenceId);
    }

    public static Notification reconstitute(Long notificationId, Long userId, NotificationType type,
                                            String message, String detail, Long referenceId, Boolean isRead, LocalDateTime createdAt) {
        return new Notification(notificationId, userId, type, message, detail, referenceId, isRead, createdAt);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}