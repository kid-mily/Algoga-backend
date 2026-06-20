package com.kidmily.algoga_server.chat.domain.repository;

import com.kidmily.algoga_server.chat.domain.model.ChatRoomMember;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository {
    ChatRoomMember save(ChatRoomMember member);
    List<ChatRoomMember> findByRoomId(Long roomId);
    List<ChatRoomMember> findByUserId(Long userId);
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
    Optional<Long> findDirectRoomIdByUserIds(Long userIdA, Long userIdB);
    void deleteByRoomIdAndUserId(Long roomId, Long userId);
    long countByRoomId(Long roomId);
}