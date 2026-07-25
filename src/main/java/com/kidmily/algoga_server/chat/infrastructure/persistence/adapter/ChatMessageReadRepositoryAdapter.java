package com.kidmily.algoga_server.chat.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.chat.domain.model.ChatMessageRead;
import com.kidmily.algoga_server.chat.domain.repository.ChatMessageReadRepository;
import com.kidmily.algoga_server.chat.infrastructure.mapper.ChatMessageReadMapper;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatMessageReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChatMessageReadRepositoryAdapter implements ChatMessageReadRepository {

    private final SpringDataChatMessageReadRepository springDataRepository;
    private final ChatMessageReadMapper chatMessageReadMapper;

    @Override
    public void saveAll(List<ChatMessageRead> reads) {
        springDataRepository.saveAll(
                reads.stream().map(chatMessageReadMapper::toJpaEntity).toList()
        );
    }

    @Override
    public void markAllAsRead(Long roomId, Long userId) {
        springDataRepository.markAllAsRead(roomId, userId);
    }

    @Override
    public long countUnreadByRoomIdAndUserId(Long roomId, Long userId) {
        return springDataRepository.countUnread(roomId, userId);
    }

    @Override
    public long countUnreadByMessageId(Long messageId) {
        return springDataRepository.countByMessageIdAndReadAtIsNull(messageId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        springDataRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByRoomIdAndUserId(Long roomId, Long userId) {
        springDataRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    @Override
    public Map<Long, Long> findUnreadCountsByRoomId(Long roomId) {
        return springDataRepository.countUnreadGroupedByRoomId(roomId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        SpringDataChatMessageReadRepository.UnreadCountRow::getMessageId,
                        SpringDataChatMessageReadRepository.UnreadCountRow::getCnt));
    }
}