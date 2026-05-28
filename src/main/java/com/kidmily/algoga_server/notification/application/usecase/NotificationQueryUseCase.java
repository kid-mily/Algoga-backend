package com.kidmily.algoga_server.notification.application.usecase;

import com.kidmily.algoga_server.notification.presentation.api.response.NotificationListResponse;

public interface NotificationQueryUseCase {
    long getUnreadCount(Long userId);
    NotificationListResponse getNotifications(Long userId, Boolean isRead, int page, int size);
}