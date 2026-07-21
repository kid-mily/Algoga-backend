package com.kidmily.algoga_server.chat.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.chat.domain.model.ChatMessage;
import com.kidmily.algoga_server.chat.domain.repository.ChatMessageRepository;
import com.kidmily.algoga_server.chat.infrastructure.mapper.ChatMessageMapper;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final SpringDataChatMessageRepository springDataRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage save(ChatMessage message) {
        return chatMessageMapper.toDomain(
                springDataRepository.save(chatMessageMapper.toJpaEntity(message))
        );
    }

    @Override
    public List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId) {
        return springDataRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(chatMessageMapper::toDomain)
                .toList();
    }


    @Override
    public Optional<ChatMessage> findLastByRoomId(Long roomId) {
        return springDataRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId)
                .map(chatMessageMapper::toDomain);
    }


}