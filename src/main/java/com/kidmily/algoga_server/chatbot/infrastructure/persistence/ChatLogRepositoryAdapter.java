package com.kidmily.algoga_server.chatbot.infrastructure.persistence;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.mapper.ChatLogMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
@RequiredArgsConstructor
public class ChatLogRepositoryAdapter implements ChatLogRepository {
    private final JpaChatLogRepository repo;
    private final ChatLogMapper mapper;
    @Override public ChatLog save(ChatLog chatLog) { return mapper.toDomain(repo.save(mapper.toJpaEntity(chatLog))); }
    @Override public List<ChatLog> findByUserId(Long userId) { return repo.findByUserIdOrderByCreatedAtAsc(userId).stream().map(mapper::toDomain).toList(); }
}