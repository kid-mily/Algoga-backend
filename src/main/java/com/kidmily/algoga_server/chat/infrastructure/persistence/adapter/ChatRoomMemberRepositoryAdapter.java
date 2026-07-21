package com.kidmily.algoga_server.chat.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.chat.domain.model.ChatRoomMember;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomMemberRepository;
import com.kidmily.algoga_server.chat.infrastructure.mapper.ChatRoomMemberMapper;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomMemberRepositoryAdapter implements ChatRoomMemberRepository {

    private final SpringDataChatRoomMemberRepository springDataRepository;
    private final ChatRoomMemberMapper chatRoomMemberMapper;

    @Override
    public ChatRoomMember save(ChatRoomMember member) {
        return chatRoomMemberMapper.toDomain(
                springDataRepository.save(chatRoomMemberMapper.toJpaEntity(member))
        );
    }

    @Override
    public List<ChatRoomMember> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).stream()
                .map(chatRoomMemberMapper::toDomain)
                .toList();
    }

    @Override
    public List<ChatRoomMember> findByUserId(Long userId) {
        return springDataRepository.findByUserId(userId).stream()
                .map(chatRoomMemberMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId) {
        return springDataRepository.findByRoomIdAndUserId(roomId, userId)
                .map(chatRoomMemberMapper::toDomain);
    }

    @Override
    public boolean existsByRoomIdAndUserId(Long roomId, Long userId) {
        return springDataRepository.existsByRoomIdAndUserId(roomId, userId);
    }

    @Override
    public Optional<Long> findDirectRoomIdByUserIds(Long userIdA, Long userIdB) {
        return springDataRepository.findDirectRoomIdByUserIds(userIdA, userIdB);
    }

    @Override
    public void deleteByRoomIdAndUserId(Long roomId, Long userId) {
        springDataRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    @Override
    public long countByRoomId(Long roomId) {
        return springDataRepository.countByRoomId(roomId);
    }
}