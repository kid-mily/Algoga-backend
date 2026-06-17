package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record FriendRequestedEvent(
        Long receiverId,
        Long requesterId,
        String requesterNickname
) implements NotificationEvent {

    @Override
    public Long getReceiverId() { return receiverId; }

    @Override
    public NotificationType getType() { return NotificationType.FRIEND_REQUESTED; }

    @Override
    public String getMessage() { return requesterNickname + "님이 친구 요청을 보냈습니다"; }

    @Override
    public String getDetail() { return null; }

    @Override
    public Long getReferenceId() { return requesterId; }
}