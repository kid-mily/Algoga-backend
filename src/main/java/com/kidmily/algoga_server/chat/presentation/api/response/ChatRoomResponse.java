package com.kidmily.algoga_server.chat.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.domain.model.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
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

        @Schema(description = "채팅방 프로필 이미지 (1:1은 상대방 프로필)")
        String profileImageUrl,

        @Schema(description = "마지막 메시지 내용")
        String lastMessage,

        @Schema(description = "마지막 메시지 시간")
        LocalDateTime lastMessageAt,

        @Schema(description = "안 읽은 메시지 수")
        int unreadCount,

        @Schema(description = "채팅방 멤버 수", example = "3")
        int memberCount


) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(), chatRoom.getType(), chatRoom.getCreatedAt(),
                chatRoom.getRoomName(), null, null, null, 0, 0
        );
    }

    public static ChatRoomResponse of(ChatRoom room, String roomName, String profileImageUrl,
                                      String lastMessage, LocalDateTime lastMessageAt, int unreadCount, int memberCount) {
        return new ChatRoomResponse(
                room.getId(), room.getType(), room.getCreatedAt(),
                roomName, profileImageUrl, lastMessage, lastMessageAt, unreadCount, memberCount
        );
    }

}