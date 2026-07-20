package com.kidmily.algoga_server.chat.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatRoomJpaEntity r SET r.roomName = :roomName WHERE r.id = :roomId")
    void updateRoomName(@Param("roomId") Long roomId, @Param("roomName") String roomName);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatRoomJpaEntity r SET r.isDeleted = true, r.deletedAt = :now WHERE r.id = :roomId")
    void softDeleteById(@Param("roomId") Long roomId, @Param("now") LocalDateTime now);

    @Query(value = "SELECT room_id FROM chat_rooms WHERE is_deleted = true AND deleted_at < :threshold",
            nativeQuery = true)
    List<Long> findExpiredRoomIds(@Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM chat_rooms WHERE room_id IN (:roomIds)", nativeQuery = true)
    void hardDeleteByIds(@Param("roomIds") List<Long> roomIds);

}