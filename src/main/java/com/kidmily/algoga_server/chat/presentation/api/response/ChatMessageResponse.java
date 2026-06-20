package com.kidmily.algoga_server.chat.presentation.api.response;

import com.kidmily.algoga_server.chat.domain.model.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 응답")
public record ChatMessageResponse(

        @Schema(description = "메시지 ID", example = "1")
        Long messageId,

        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "발신자 유저 ID", example = "1")
        Long senderId,

        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @Schema(description = "안 읽은 사람 수", example = "2")
        int unreadCount,

        @Schema(description = "메시지 전송 시각")
        LocalDateTime createdAt
) {
    public static ChatMessageResponse of(ChatMessage message, int unreadCount) {
        return new ChatMessageResponse(
                message.getId(), message.getRoomId(), message.getSenderId(),
                message.getContent(), unreadCount, message.getCreatedAt()
        );
    }
}