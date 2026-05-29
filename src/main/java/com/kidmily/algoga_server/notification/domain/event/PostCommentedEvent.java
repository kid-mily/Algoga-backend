package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record PostCommentedEvent(
        Long receiverId,        // 게시글 작성자
        Long commenterId,       // 댓글 작성자
        String commenterNickname,
        Long postId,
        String commentContent
) implements NotificationEvent {

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.POST_COMMENTED;
    }

    @Override
    public String getMessage() {
        return commenterNickname + "님이 내 게시글에 댓글을 달았습니다";
    }

    @Override
    public String getDetail() { return commentContent; }

    @Override
    public Long getReferenceId() { return postId; }
}