package com.kidmily.algoga_server.chat.application.usecase;

import com.kidmily.algoga_server.chat.application.command.CreateChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.CreateGroupChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.SendChatMessageCommand;
import com.kidmily.algoga_server.chat.domain.model.ChatMessage;
import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatMessageResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomResponse;

import java.util.List;

public interface ChatUseCase {

    ChatRoomResponse getOrCreateRoom(CreateChatRoomCommand command);
    List<ChatMessageResponse> getMessages(Long roomId, Long userId);
    ChatMessageResponse sendMessage(SendChatMessageCommand command);
    void markAsRead(Long roomId, Long userId);
    List<ChatRoomResponse> getRooms(Long userId);
    ChatRoomResponse createGroupRoom(CreateGroupChatRoomCommand command);
    void leaveRoom(Long roomId, Long userId);
}