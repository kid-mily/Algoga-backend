package com.kidmily.algoga_server.notification.application.usecase;

public interface NotificationQueryUseCase {
    long getUnreadCount(Long userId);
}