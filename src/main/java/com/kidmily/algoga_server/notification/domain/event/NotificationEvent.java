package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public interface NotificationEvent {
    Long getReceiverId();        // 알림 받을 사람
    NotificationType getType();  // 알림 타입
    String getMessage();         // 알림 메시지
}