package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record NoticeCreatedEvent(
        Long receiverId,
        Long noticeId,
        String noticeTitle,
        String noticeContent
) implements NotificationEvent {

    @Override
    public Long getReceiverId() { return receiverId; }

    @Override
    public NotificationType getType() { return NotificationType.NOTICE_CREATED; }

    @Override
    public String getMessage() { return "새 공지사항: " + noticeTitle; }

    @Override
    public String getDetail() { return noticeContent; }

    @Override
    public Long getReferenceId() { return noticeId; }
}