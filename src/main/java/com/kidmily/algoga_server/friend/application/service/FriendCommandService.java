package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.command.CreateFriendCommand;
import com.kidmily.algoga_server.friend.application.usecase.FriendCommandUseCase;
import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
import com.kidmily.algoga_server.global.event.FriendBlockedEvent;
import com.kidmily.algoga_server.notification.domain.event.FriendAcceptedEvent;
import com.kidmily.algoga_server.notification.domain.event.FriendRequestedEvent;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendCommandService implements FriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 통계 대시보드용 커스텀 지표
    private final Counter friendRequestSentTotal;
    private final Counter friendRequestAcceptedTotal;
    private final Counter friendRequestRejectedTotal;
    private final Counter friendBlockedTotal;

    @Override
    public void sendFriendRequest(CreateFriendCommand command) {
        Long myId = command.requesterId();

        User targetUser = userRepository.findByPersonalCode(command.targetUserCode())
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        if (targetUser.getId().equals(myId)) {
            throw new FriendException(FriendErrorCode.CANNOT_ADD_SELF);
        }

        // findRelationBetween 하나로 양방향을 다 커버하므로, 차단 여부와 기존 관계 상태를 같은 조회 결과로 판별한다
        // (예전엔 차단 체크용 조회를 따로 한 번 더 날려서 같은 쌍을 두 번 조회했음)
        Optional<FriendRelation> existingRelation = friendRepository.findRelationBetween(myId, targetUser.getId());
        if (existingRelation.isPresent()) {
            FriendRelation relation = existingRelation.get();
            RelationStatus status = relation.getStatus();
            if (status == RelationStatus.BLOCKED) {
                if (relation.getRequesterId().equals(targetUser.getId())) {
                    throw new FriendException(FriendErrorCode.BLOCKED_BY_TARGET); // 상대가 나를 차단
                }
                throw new FriendException(FriendErrorCode.ALREADY_BLOCKED); // 내가 상대를 차단
            }
            if (status == RelationStatus.ACCEPTED) throw new FriendException(FriendErrorCode.ALREADY_FRIEND);
            if (status == RelationStatus.REQUESTED) throw new FriendException(FriendErrorCode.ALREADY_REQUESTED);
        }

        // 친구를 요청하기 직전에 내 친구가 100명인지 DB에 count를 날려 확인
        if (friendRepository.countAcceptedFriends(myId) >= 100) {
            throw new FriendException(FriendErrorCode.FRIEND_LIMIT_EXCEEDED);
        }

        // 받는 사람의 대기 중인 요청함이 무제한으로 쌓이는 것을 방지 (누군가 무제한 요청을 보내는 어뷰징 방지)
        if (friendRepository.countPendingRequests(targetUser.getId()) >= 100) {
            throw new FriendException(FriendErrorCode.RECEIVED_REQUEST_LIMIT_EXCEEDED);
        }

        FriendRelation newRelation = FriendRelation.builder()
                .requesterId(myId)
                .receiverId(targetUser.getId())
                .status(RelationStatus.REQUESTED)
                .build();
        friendRepository.save(newRelation);

        User requester = userRepository.findById(myId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));
        eventPublisher.publishEvent(new FriendRequestedEvent(
                targetUser.getId(),
                myId,
                requester.getNickname()
        ));
        friendRequestSentTotal.increment();
    }

    @Override
    public void acceptFriendRequest(Long myId, Long requestId) {
        FriendRelation relation = friendRepository.findById(requestId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.REQUEST_NOT_FOUND));

        if (!relation.getReceiverId().equals(myId) || relation.getStatus() != RelationStatus.REQUESTED) {
            throw new FriendException(FriendErrorCode.REQUEST_NOT_FOUND);
        }

        // 요청을 수락하기 직전에 나와 상대방 중 한 명이라도 친구가 100명이 넘는지 확인.
        // count 쿼리를 각자 따로 2번 날리는 대신, 두 사람이 걸린 ACCEPTED 관계를 한 번에 배치조회해서 각자 세어본다.
        Long requesterId = relation.getRequesterId();
        List<FriendRelation> acceptedRelations = friendRepository.findAcceptedFriendsAmong(List.of(myId, requesterId));
        long myFriendCount = acceptedRelations.stream()
                .filter(r -> r.getRequesterId().equals(myId) || r.getReceiverId().equals(myId))
                .count();
        long requesterFriendCount = acceptedRelations.stream()
                .filter(r -> r.getRequesterId().equals(requesterId) || r.getReceiverId().equals(requesterId))
                .count();
        if (myFriendCount >= 100 || requesterFriendCount >= 100) {
            throw new FriendException(FriendErrorCode.FRIEND_LIMIT_EXCEEDED);
        }

        relation.accept();
        friendRepository.save(relation);

        User acceptor = userRepository.findById(myId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));
        eventPublisher.publishEvent(new FriendAcceptedEvent(
                relation.getRequesterId(),
                myId,
                acceptor.getNickname()
        ));
        friendRequestAcceptedTotal.increment();
    }

    @Override
    public void rejectFriendRequest(Long myId, Long relationId) {
        FriendRelation relation = friendRepository.findById(relationId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (!relation.getReceiverId().equals(myId)) {
            throw new FriendException(FriendErrorCode.UNAUTHORIZED_ACTION);
        }
        friendRepository.deleteById(relationId);
        friendRequestRejectedTotal.increment();
    }

    @Override
    public void deleteFriend(Long myId, Long relationId) {
        FriendRelation relation = friendRepository.findById(relationId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (!relation.getRequesterId().equals(myId) && !relation.getReceiverId().equals(myId)) {
            throw new FriendException(FriendErrorCode.UNAUTHORIZED_ACTION);
        }
        friendRepository.deleteById(relation.getId());
    }

    @Override
    public void blockUser(CreateFriendCommand command) {
        Long myId = command.requesterId();

        User targetUser = userRepository.findByPersonalCode(command.targetUserCode())
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        Optional<FriendRelation> existingRelation = friendRepository.findRelationBetween(myId, targetUser.getId());

        if (existingRelation.isPresent()) {
            FriendRelation relation = existingRelation.get();
            if (relation.getStatus() == RelationStatus.BLOCKED && relation.getRequesterId().equals(myId)) {
                throw new FriendException(FriendErrorCode.ALREADY_BLOCKED);
            }
            // 기존 관계 row를 지웠다가 새로 만드는 대신(DELETE+INSERT), 같은 row를 차단 상태로 갱신(UPDATE 1번)
            friendRepository.reassignAsBlocked(relation.getId(), myId, targetUser.getId());
        } else {
            FriendRelation blockRelation = FriendRelation.builder()
                    .requesterId(myId)
                    .receiverId(targetUser.getId())
                    .status(RelationStatus.BLOCKED)
                    .build();
            friendRepository.save(blockRelation);
        }

        // 차단 시 1:1 채팅방은 삭제하고 그룹 채팅방은 유지해야 함 -> chat 도메인이 구독해서 처리
        eventPublisher.publishEvent(new FriendBlockedEvent(myId, targetUser.getId()));
        friendBlockedTotal.increment();
    }

    @Override
    public void toggleFavorite(Long myId, Long relationId) {
        FriendRelation relation = friendRepository.findById(relationId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (!relation.getRequesterId().equals(myId) && !relation.getReceiverId().equals(myId)) {
            throw new FriendException(FriendErrorCode.UNAUTHORIZED_ACTION);
        }

        if (relation.getStatus() != RelationStatus.ACCEPTED) {
            throw new FriendException(FriendErrorCode.INVALID_STATUS);
        }

        relation.toggleFavorite();
        friendRepository.save(relation);
    }

    @Override
    public void unblockUser(Long myId, String targetUserCode) {
        User targetUser = userRepository.findByPersonalCode(targetUserCode)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        FriendRelation relation = friendRepository.findByRequesterIdAndReceiverId(myId, targetUser.getId())
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (relation.getStatus() != RelationStatus.BLOCKED) {
            throw new FriendException(FriendErrorCode.INVALID_STATUS);
        }

        // 차단 해제 시 다시 친구 요청 없이 바로 친구 관계로 복원
        relation.accept();
        friendRepository.save(relation);
    }
}