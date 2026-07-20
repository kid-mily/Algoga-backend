package com.kidmily.algoga_server.chat.domain.repository;

import com.kidmily.algoga_server.chat.domain.model.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom chatRoom);
    Optional<ChatRoom> findById(Long id);
    void softDelete(Long roomId);
    void updateRoomName(Long roomId, String roomName);


}