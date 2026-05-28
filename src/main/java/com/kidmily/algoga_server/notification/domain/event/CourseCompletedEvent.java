package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record CourseCompletedEvent(
        Long receiverId,
        String courseName
) implements NotificationEvent {

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.COURSE_COMPLETED;
    }

    @Override
    public String getMessage() {
        return "강좌 수강 완료: " + courseName;
    }
}