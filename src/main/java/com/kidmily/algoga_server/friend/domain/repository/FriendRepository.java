package com.kidmily.algoga_server.friend.domain.repository;

import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import java.util.List;
import java.util.Optional;

public interface FriendRepository {
    FriendRelation save(FriendRelation relation);
    Optional<FriendRelation> findById(Long id);
    void deleteById(Long id);

    // 비즈니스 쿼리
    Optional<FriendRelation> findRelationBetween(Long user1Id, Long user2Id);
    long countAcceptedFriends(Long userId);
    List<FriendRelation> findAcceptedFriends(Long userId);
    List<FriendRelation> findByReceiverIdAndStatus(Long receiverId, RelationStatus status);

    // 차단 해제 시 필요
    Optional<FriendRelation> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    // 삭제 시 필요 (이미 save/findById가 있다면 추가)
    void delete(FriendRelation relation);

}