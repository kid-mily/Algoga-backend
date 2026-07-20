package com.kidmily.algoga_server.chat.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, Long> {

    List<ChatMessageJpaEntity> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    Optional<ChatMessageJpaEntity> findTopByRoomIdOrderByCreatedAtDesc(Long roomId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChatMessageJpaEntity m WHERE m.roomId IN :roomIds")
    void deleteByRoomIdIn(@Param("roomIds") List<Long> roomIds);

}