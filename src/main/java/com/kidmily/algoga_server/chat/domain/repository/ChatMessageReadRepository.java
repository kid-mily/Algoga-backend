package com.kidmily.algoga_server.chat.domain.repository;

import com.kidmily.algoga_server.chat.domain.model.ChatMessageRead;
import java.util.List;

public interface ChatMessageReadRepository {
    void saveAll(List<ChatMessageRead> reads);
    void markAllAsRead(Long roomId, Long userId);
    long countUnreadByRoomIdAndUserId(Long roomId, Long userId);
    long countUnreadByMessageId(Long messageId);
    void deleteByUserId(Long userId);
}