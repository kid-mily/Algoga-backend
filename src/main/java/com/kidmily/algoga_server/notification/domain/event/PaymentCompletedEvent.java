package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record PaymentCompletedEvent(
        Long receiverId,
        String itemName,
        int amount
) implements NotificationEvent {

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PAYMENT_COMPLETED;
    }

    @Override
    public String getMessage() {
        return "결제 완료: " + itemName + " " + amount + "원";
    }
}