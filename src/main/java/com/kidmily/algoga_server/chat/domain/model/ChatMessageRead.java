package com.kidmily.algoga_server.chat.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageRead {

    private Long id;
    private Long messageId;
    private Long userId;
    private LocalDateTime readAt;

    private ChatMessageRead(Long messageId, Long userId) {
        this.messageId = messageId;
        this.userId = userId;
    }

    private ChatMessageRead(Long id, Long messageId, Long userId, LocalDateTime readAt) {
        this.id = id;
        this.messageId = messageId;
        this.userId = userId;
        this.readAt = readAt;
    }

    public static ChatMessageRead create(Long messageId, Long userId) {
        return new ChatMessageRead(messageId, userId);
    }

    public static ChatMessageRead reconstitute(Long id, Long messageId, Long userId, LocalDateTime readAt) {
        return new ChatMessageRead(id, messageId, userId, readAt);
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }
}