package com.kidmily.algoga_server.friend.infrastructure.persistence.repository;

import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.infrastructure.persistence.entity.FriendJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataFriendRepository extends JpaRepository<FriendJpaEntity, Long> {

    @Query("SELECT f FROM FriendJpaEntity f WHERE (f.requesterId = :u1 AND f.receiverId = :u2) OR (f.requesterId = :u2 AND f.receiverId = :u1)")
    Optional<FriendJpaEntity> findRelationBetween(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("SELECT count(f) FROM FriendJpaEntity f WHERE (f.requesterId = :userId OR f.receiverId = :userId) AND f.status = 'ACCEPTED'")
    long countAcceptedFriends(@Param("userId") Long userId);

    @Query("SELECT f FROM FriendJpaEntity f WHERE (f.requesterId = :userId OR f.receiverId = :userId) AND f.status = 'ACCEPTED'")
    List<FriendJpaEntity> findAcceptedFriends(@Param("userId") Long userId);

    @Query("SELECT f FROM FriendJpaEntity f WHERE f.receiverId = :receiverId AND f.status = :status")
    List<FriendJpaEntity> findByReceiverIdAndStatus(@Param("receiverId") Long receiverId, @Param("status") RelationStatus status);

    @Query("SELECT f FROM FriendJpaEntity f WHERE f.requesterId = :requesterId AND f.status = :status")
    List<FriendJpaEntity> findByRequesterIdAndStatus(@Param("requesterId") Long requesterId, @Param("status") RelationStatus status);

    @Query("SELECT f FROM FriendJpaEntity f WHERE f.requesterId = :requesterId AND f.receiverId = :receiverId")
    Optional<FriendJpaEntity> findByRequesterIdAndReceiverId(@Param("requesterId") Long requesterId, @Param("receiverId") Long receiverId);

    @Modifying
    @Query("DELETE FROM FriendJpaEntity f WHERE f.requesterId = :userId OR f.receiverId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}