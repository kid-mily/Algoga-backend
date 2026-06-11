package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.mapper.ChatLogMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ChatLogEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatLogRepositoryAdapter implements ChatLogRepository {

    private final JpaChatLogRepository jpaChatLogRepository;
    private final ChatLogMapper chatLogMapper;

    @Override
    public ChatLog save(ChatLog chatLog) {
        ChatLogEntity entity = chatLogMapper.toJpaEntity(chatLog);
        return chatLogMapper.toDomain(jpaChatLogRepository.save(entity));
    }

    @Override
    public List<ChatLog> findByUserId(Long userId, Long beforeChatLogId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<ChatLogEntity> entities;
        if (beforeChatLogId == null) {
            entities = jpaChatLogRepository.findByUserIdOrderByCreatedAtAsc(userId, pageable);
        } else {
            entities = jpaChatLogRepository.findByUserIdAndChatLogIdLessThanOrderByCreatedAtAsc(userId, beforeChatLogId, pageable);
        }
        return entities.stream().map(chatLogMapper::toDomain).toList();
    }
}