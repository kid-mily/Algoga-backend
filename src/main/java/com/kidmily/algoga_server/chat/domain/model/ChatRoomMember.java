package com.kidmily.algoga_server.chat.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    private Long id;
    private Long roomId;
    private Long userId;
    private LocalDateTime joinedAt;

    private ChatRoomMember(Long roomId, Long userId) {
        this.roomId = roomId;
        this.userId = userId;
        this.joinedAt = LocalDateTime.now();
    }

    private ChatRoomMember(Long id, Long roomId, Long userId, LocalDateTime joinedAt) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public static ChatRoomMember create(Long roomId, Long userId) {
        return new ChatRoomMember(roomId, userId);
    }

    public static ChatRoomMember reconstitute(Long id, Long roomId, Long userId, LocalDateTime joinedAt) {
        return new ChatRoomMember(id, roomId, userId, joinedAt);
    }
}