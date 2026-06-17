package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record QnaAnsweredEvent(
        Long receiverId,
        String answererNickname,
        Long qnaId,
        String answerContent
) implements NotificationEvent {

    @Override
    public Long getReceiverId() { return receiverId; }

    @Override
    public NotificationType getType() { return NotificationType.QNA_ANSWERED; }

    @Override
    public String getMessage() { return answererNickname + "님이 Q&A에 답변을 등록했습니다"; }

    @Override
    public String getDetail() { return answerContent; }

    @Override
    public Long getReferenceId() { return qnaId; }
}