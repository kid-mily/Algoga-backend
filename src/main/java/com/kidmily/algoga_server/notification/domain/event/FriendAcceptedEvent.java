package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record FriendAcceptedEvent(
        Long receiverId,
        Long acceptorId,
        String acceptorNickname
) implements NotificationEvent {

    @Override
    public Long getReceiverId() { return receiverId; }

    @Override
    public NotificationType getType() { return NotificationType.FRIEND_ACCEPTED; }

    @Override
    public String getMessage() { return acceptorNickname + "님이 친구 요청을 수락했습니다"; }

    @Override
    public String getDetail() { return null; }

    @Override
    public Long getReferenceId() { return acceptorId; }
}