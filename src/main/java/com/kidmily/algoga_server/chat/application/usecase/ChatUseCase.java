package com.kidmily.algoga_server.chat.application.usecase;

import com.kidmily.algoga_server.chat.application.command.CreateChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.CreateGroupChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.SendChatMessageCommand;
import com.kidmily.algoga_server.chat.domain.model.ChatMessage;
import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatMessageResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomMemberResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomResponse;

import java.util.List;
import java.util.Optional;

public interface ChatUseCase {

    ChatRoomResponse getOrCreateRoom(CreateChatRoomCommand command);
    List<ChatMessageResponse> getMessages(Long roomId, Long userId);
    ChatMessageResponse sendMessage(SendChatMessageCommand command);
    void markAsRead(Long roomId, Long userId);
    List<ChatRoomResponse> getRooms(Long userId);
    ChatRoomResponse createGroupRoom(CreateGroupChatRoomCommand command);
    Optional<ChatMessageResponse> leaveRoom(Long roomId, Long userId);
    List<Long> getRoomMemberIds(Long roomId);
    int getUnreadCount(Long roomId, Long userId);
    String getNickname(Long userId);
    List<ChatRoomMemberResponse> getRoomMembers(Long roomId, Long userId);
    ChatRoomResponse addMembers(Long roomId, Long requesterId, List<Long> targetUserIds);
    void renameRoom(Long roomId, Long requesterId, String roomName);
}