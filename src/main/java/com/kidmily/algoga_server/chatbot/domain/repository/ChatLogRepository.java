package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import java.util.List;

public interface ChatLogRepository {
    ChatLog save(ChatLog chatLog);
    List<ChatLog> findByUserId(Long userId, Long beforeChatLogId, int limit);
}