package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record InquiryAnsweredEvent(
        Long receiverId,
        Long inquiryId,
        String answerContent
) implements NotificationEvent {

    @Override
    public Long getReceiverId() { return receiverId; }

    @Override
    public NotificationType getType() { return NotificationType.INQUIRY_ANSWERED; }

    @Override
    public String getMessage() { return "문의하신 내용에 답변이 등록되었습니다"; }

    @Override
    public String getDetail() { return answerContent; }

    @Override
    public Long getReferenceId() { return inquiryId; }
}