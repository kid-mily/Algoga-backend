package com.kidmily.algoga_server.notification.domain.event;

import com.kidmily.algoga_server.notification.domain.model.NotificationType;

public record CommentRepliedEvent(
        Long receiverId,        // 부모 댓글 작성자
        Long replierId,         // 대댓글 작성자
        String replierNickname,
        Long postId,
        Long parentCommentId,
        String replyContent
) implements NotificationEvent {

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.COMMENT_REPLIED;
    }

    @Override
    public String getMessage() {
        return replierNickname + "님이 내 댓글에 답글을 달았습니다";
    }

    @Override
    public String getDetail() { return replyContent; }

    @Override
    public Long getReferenceId() { return postId; }
}