package com.kidmily.algoga_server.chat.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatMessageReadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataChatMessageReadRepository extends JpaRepository<ChatMessageReadJpaEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ChatMessageReadJpaEntity r SET r.readAt = CURRENT_TIMESTAMP
        WHERE r.userId = :userId
        AND r.messageId IN (
            SELECT m.id FROM ChatMessageJpaEntity m WHERE m.roomId = :roomId
        )
        AND r.readAt IS NULL
    """)
    void markAllAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("""
        SELECT COUNT(r) FROM ChatMessageReadJpaEntity r
        WHERE r.userId = :userId
        AND r.messageId IN (
            SELECT m.id FROM ChatMessageJpaEntity m WHERE m.roomId = :roomId
        )
        AND r.readAt IS NULL
    """)
    long countUnread(@Param("roomId") Long roomId, @Param("userId") Long userId);
    long countByMessageIdAndReadAtIsNull(Long messageId);
    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChatMessageReadJpaEntity r WHERE r.userId = :userId AND r.messageId IN (SELECT m.id FROM ChatMessageJpaEntity m WHERE m.roomId = :roomId)")
    void deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("""
    DELETE FROM ChatMessageReadJpaEntity r
    WHERE r.messageId IN (
        SELECT m.id FROM ChatMessageJpaEntity m WHERE m.roomId IN :roomIds
    )
""")
    void deleteByRoomIdIn(@Param("roomIds") List<Long> roomIds);

}