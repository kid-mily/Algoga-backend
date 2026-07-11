package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.command.CreateFriendCommand;
import com.kidmily.algoga_server.friend.application.usecase.FriendCommandUseCase;
import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
// import com.kidmily.algoga_server.friend.settings.cache.FriendCacheType;
import com.kidmily.algoga_server.notification.domain.event.FriendAcceptedEvent;
import com.kidmily.algoga_server.notification.domain.event.FriendRequestedEvent;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendCommandService implements FriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void sendFriendRequest(CreateFriendCommand command) {
        Long myId = command.requesterId();

        User targetUser = userRepository.findByPersonalCode(command.targetUserCode())
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        if (targetUser.getId().equals(myId)) {
            throw new FriendException(FriendErrorCode.CANNOT_ADD_SELF);
        }

        Optional<FriendRelation> blockCheck = friendRepository.findByRequesterIdAndReceiverId(targetUser.getId(), myId);
        if (blockCheck.isPresent() && blockCheck.get().getStatus() == RelationStatus.BLOCKED) {
            throw new FriendException(FriendErrorCode.BLOCKED_BY_TARGET);
        }

        Optional<FriendRelation> existingRelation = friendRepository.findRelationBetween(myId, targetUser.getId());
        if (existingRelation.isPresent()) {
            RelationStatus status = existingRelation.get().getStatus();
            if (status == RelationStatus.ACCEPTED) throw new FriendException(FriendErrorCode.ALREADY_FRIEND);
            if (status == RelationStatus.REQUESTED) throw new FriendException(FriendErrorCode.ALREADY_REQUESTED);
            if (status == RelationStatus.BLOCKED) throw new FriendException(FriendErrorCode.ALREADY_BLOCKED);
        }

        // 친구를 요청하기 직전에 내 친구가 100명인지 DB에 count를 날려 확인
        if (friendRepository.countAcceptedFriends(myId) >= 100) {
            throw new FriendException(FriendErrorCode.FRIEND_LIMIT_EXCEEDED);
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
    }

    @Override
    public void acceptFriendRequest(Long myId, Long requestId) {
        FriendRelation relation = friendRepository.findById(requestId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.REQUEST_NOT_FOUND));

        if (!relation.getReceiverId().equals(myId) || relation.getStatus() != RelationStatus.REQUESTED) {
            throw new FriendException(FriendErrorCode.REQUEST_NOT_FOUND);
        }

        // 요청을 수락하기 직전에 나와 상대방 중 한 명이라도 친구가 100명이 넘는지
        // 확인하기 위해 DB에 count 쿼리를 2번이나 날리고 있음
        if (friendRepository.countAcceptedFriends(myId) >= 100 || friendRepository.countAcceptedFriends(relation.getRequesterId()) >= 100) {
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
    }

    @Override
    public void rejectFriendRequest(Long myId, Long relationId) {
        FriendRelation relation = friendRepository.findById(relationId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (!relation.getReceiverId().equals(myId)) {
            throw new FriendException(FriendErrorCode.UNAUTHORIZED_ACTION);
        }
        friendRepository.deleteById(relationId);
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
            friendRepository.deleteById(relation.getId());
        }

        FriendRelation blockRelation = FriendRelation.builder()
                .requesterId(myId)
                .receiverId(targetUser.getId())
                .status(RelationStatus.BLOCKED)
                .build();
        friendRepository.save(blockRelation);
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
        friendRepository.deleteById(relation.getId());
    }
}