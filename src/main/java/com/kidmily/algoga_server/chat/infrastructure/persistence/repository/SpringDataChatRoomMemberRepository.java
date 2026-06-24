package com.kidmily.algoga_server.chat.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatRoomMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface SpringDataChatRoomMemberRepository extends JpaRepository<ChatRoomMemberJpaEntity, Long> {

    List<ChatRoomMemberJpaEntity> findByRoomId(Long roomId);
    List<ChatRoomMemberJpaEntity> findByUserId(Long userId);
    Optional<ChatRoomMemberJpaEntity> findByRoomIdAndUserId(Long roomId, Long userId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
    @Modifying
    @Query("DELETE FROM ChatRoomMemberJpaEntity m WHERE m.roomId = :roomId AND m.userId = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
    long countByRoomId(Long roomId);

    @Query("""
        SELECT m.roomId FROM ChatRoomMemberJpaEntity m
        WHERE m.userId = :userIdA
        AND m.roomId IN (
            SELECT m2.roomId FROM ChatRoomMemberJpaEntity m2
            WHERE m2.userId = :userIdB
        )
        AND m.roomId IN (
            SELECT m3.roomId FROM ChatRoomMemberJpaEntity m3
            GROUP BY m3.roomId HAVING COUNT(m3.roomId) = 2
        )
    """)
    Optional<Long> findDirectRoomIdByUserIds(@Param("userIdA") Long userIdA,
                                             @Param("userIdB") Long userIdB);
}