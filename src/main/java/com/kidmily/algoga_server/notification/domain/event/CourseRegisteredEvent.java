package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record CourseRegisteredEvent(
        Long receiverId,
        String courseName
) implements NotificationEvent {

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.COURSE_REGISTERED;
    }

    @Override
    public String getMessage() {
        return "강좌 수강 등록 완료: " + courseName;
    }

    @Override
    public String getDetail() {
        return "";
    }

    @Override
    public Long getReferenceId() { return null; }
}