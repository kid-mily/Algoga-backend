package com.kidmily.algoga_server.chat.domain.model;

import com.kidmily.algoga_server.chat.exception.ChatErrorCode;
import com.kidmily.algoga_server.chat.exception.ChatException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    private Long id;
    private ChatRoomType type;
    private LocalDateTime createdAt;
    private boolean isDeleted;
    private String roomName;
    private static final int MAX_ROOM_NAME_LENGTH = 20;
    private static final int MAX_GROUP_MEMBERS = 100;

    private ChatRoom(ChatRoomType type, String roomName) {
        this.type = type;
        this.roomName = roomName;
        this.createdAt = LocalDateTime.now();
    }

    private ChatRoom(Long id, ChatRoomType type, LocalDateTime createdAt, boolean isDeleted, String roomName) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.roomName = roomName;
    }

    public static ChatRoom create(ChatRoomType type, String roomName, int invitedCount) {
        ChatRoom room = new ChatRoom(type, roomName);
        if (type == ChatRoomType.GROUP) {
            room.validateGroupSize(invitedCount);
        }
        return room;
    }

    private void validateGroupSize(int invitedCount) {
        if (invitedCount < 2) {
            throw new ChatException(ChatErrorCode.CHAT_GROUP_MIN_MEMBERS);
        }
        if (invitedCount > MAX_GROUP_MEMBERS) {
            throw new ChatException(ChatErrorCode.CHAT_GROUP_MAX_MEMBERS);
        }
    }

    public static ChatRoom reconstitute(Long id, ChatRoomType type, LocalDateTime createdAt, boolean isDeleted, String roomName) {
        return new ChatRoom(id, type, createdAt, isDeleted, roomName);
    }

    public void delete() {
        this.isDeleted = true;

    }

    public void validateCanAddMembers(int currentCount, int addCount) {
        if (currentCount + addCount > MAX_GROUP_MEMBERS) {
            throw new ChatException(ChatErrorCode.CHAT_GROUP_MAX_MEMBERS);
        }
    }

    private void validateRoomName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NAME_BLANK);
        }
        if (roomName.length() > MAX_ROOM_NAME_LENGTH) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NAME_TOO_LONG);
        }
    }


}