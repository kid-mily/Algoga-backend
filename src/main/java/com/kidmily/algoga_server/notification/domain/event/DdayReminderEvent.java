package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record DdayReminderEvent(
        Long receiverId,
        String courseName,
        Long courseId
) implements NotificationEvent {

    @Override
    public Long getReceiverId() { return receiverId; }

    @Override
    public NotificationType getType() { return NotificationType.DDAY_REMINDER; }

    @Override
    public String getMessage() {
        return "\"" + courseName + "\" 수강 기간이 7일 남았습니다";
    }

    @Override
    public String getDetail() {
        return "수강 기간이 만료되면 강의를 시청할 수 없습니다.";
    }

    @Override
    public Long getReferenceId() { return courseId; }
}