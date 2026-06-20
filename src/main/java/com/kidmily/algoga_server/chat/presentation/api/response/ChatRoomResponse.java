package com.kidmily.algoga_server.chat.presentation.api.response;

import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.domain.model.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 응답")
public record ChatRoomResponse(

        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "채팅방 타입 (DIRECT / GROUP)", example = "DIRECT")
        ChatRoomType type,

        @Schema(description = "채팅방 생성일시")
        LocalDateTime createdAt,

        @Schema(description = "채팅방 이름")
        String roomName,

        @Schema(description = "마지막 메시지 내용")
        String lastMessage,

        @Schema(description = "마지막 메시지 시간")
        LocalDateTime lastMessageAt,

        @Schema(description = "안 읽은 메시지 수")
        int unreadCount


) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getType(),
                chatRoom.getCreatedAt(),
                chatRoom.getRoomName(),
                null,
                null,
                0
        );
    }

    public static ChatRoomResponse of(ChatRoom room, String lastMessage,
                                      LocalDateTime lastMessageAt, int unreadCount) {
        return new ChatRoomResponse(
                room.getId(), room.getType(), room.getCreatedAt(),
                room.getRoomName(), lastMessage, lastMessageAt, unreadCount
        );
    }
}