package com.kidmily.algoga_server.notification.application.usecase;

public interface NotificationCommandUseCase {
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long userId, Long notificationId);
    void deleteAllNotifications(Long userId);
}