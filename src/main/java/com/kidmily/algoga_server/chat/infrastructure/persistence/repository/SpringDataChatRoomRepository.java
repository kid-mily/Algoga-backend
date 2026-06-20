package com.kidmily.algoga_server.chat.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatRoomJpaEntity r SET r.isDeleted = true WHERE r.id = :roomId")
    void softDeleteById(@Param("roomId") Long roomId);

}