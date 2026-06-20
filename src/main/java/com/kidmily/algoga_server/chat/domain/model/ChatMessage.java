package com.kidmily.algoga_server.chat.domain.model;

import com.kidmily.algoga_server.chat.exception.ChatErrorCode;
import com.kidmily.algoga_server.chat.exception.ChatException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    private static final int MAX_CONTENT_LENGTH = 300;

    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    private ChatMessage(Long roomId, Long senderId, String content) {
        validateContent(content);
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    private ChatMessage(Long id, Long roomId, Long senderId, String content,
                  LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatMessage create(Long roomId, Long senderId, String content) {
        return new ChatMessage(roomId, senderId, content);
    }

    public static ChatMessage reconstitute(Long id, Long roomId, Long senderId,
                                           String content, LocalDateTime createdAt) {
        return new ChatMessage(id, roomId, senderId, content, createdAt);
    }


    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ChatException(ChatErrorCode.CHAT_CONTENT_BLANK);
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ChatException(ChatErrorCode.CHAT_CONTENT_TOO_LONG);
        }
    }
}