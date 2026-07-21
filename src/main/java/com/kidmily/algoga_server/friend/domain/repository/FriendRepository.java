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
    List<FriendRelation> findByRequesterIdAndStatus(Long requesterId, RelationStatus status);

    // 여러 명의 친구 한도(100명) 초과 여부를 한 번에 확인하기 위한 배치조회
    // (예: 친구 요청 수락 시 나/상대방 두 명을 매번 각각 count 쿼리로 따로 조회하던 것을 1번으로 합침)
    List<FriendRelation> findAcceptedFriendsAmong(List<Long> userIds);

    // 받은 친구 요청 개수 상한 체크용 (누군가 무제한으로 요청을 보내 목록이 무한정 쌓이는 것 방지)
    long countPendingRequests(Long receiverId);

    // 차단 처리 시 기존 관계 row를 DELETE+INSERT 하지 않고 그대로 재사용(UPDATE)하기 위함
    void reassignAsBlocked(Long relationId, Long blockerId, Long blockedId);

    // 차단 해제 시 필요
    Optional<FriendRelation> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    // 유저 탈퇴 시 필요 (요청자/수신자 어느 쪽이든 전부 삭제)
    void deleteAllByUserId(Long userId);

}