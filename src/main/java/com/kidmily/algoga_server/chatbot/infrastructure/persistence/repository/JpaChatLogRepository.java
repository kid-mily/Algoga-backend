package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ChatLogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaChatLogRepository extends JpaRepository<ChatLogEntity, Long> {
    List<ChatLogEntity> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);
    List<ChatLogEntity> findByUserIdAndChatLogIdLessThanOrderByCreatedAtAsc(Long userId, Long beforeChatLogId, Pageable pageable);
}