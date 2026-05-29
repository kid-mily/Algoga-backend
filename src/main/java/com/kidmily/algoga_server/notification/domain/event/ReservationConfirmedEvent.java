package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record ReservationConfirmedEvent(
        Long receiverId,
        String reservationName
) implements NotificationEvent {
    @Override
    public String getDetail() {
        return "";
    }

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.RESERVATION_CONFIRMED;
    }

    @Override
    public String getMessage() {
        return "예약 확정: " + reservationName;
    }

    @Override
    public Long getReferenceId() { return null; }
}