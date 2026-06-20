package com.kidmily.algoga_server.chat.domain.repository;

import com.kidmily.algoga_server.chat.domain.model.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    Optional<ChatMessage> findLastByRoomId(Long roomId);
}