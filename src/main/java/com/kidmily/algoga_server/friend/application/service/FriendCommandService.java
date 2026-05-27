package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.usecase.FriendCommandUseCase;
import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendCommandService implements FriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public void sendFriendRequest(Long myId, String targetPersonalCode) {
        // 대상 유저 정보 가져오기
        User targetUser = userRepository.findByPersonalCode(targetPersonalCode)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        // 본인 추가 방지
        if (targetUser.getId().equals(myId)) {
            throw new FriendException(FriendErrorCode.CANNOT_ADD_SELF);
        }

        // 상대방이 나를 차단했는지 확인
        // (상대방이 requester이고, 내가 receiver인 차단 관계가 있는지 확인)
        Optional<FriendRelation> blockCheck = friendRepository.findByRequesterIdAndReceiverId(targetUser.getId(), myId);
        if (blockCheck.isPresent() && blockCheck.get().getStatus() == RelationStatus.BLOCKED) {
            throw new FriendException(FriendErrorCode.BLOCKED_BY_TARGET);
        }

        // 4. 기존 관계 검사 (이미 친구인지, 이미 요청했는지 등)
        Optional<FriendRelation> existingRelation = friendRepository.findRelationBetween(myId, targetUser.getId());
        if (existingRelation.isPresent()) {
            RelationStatus status = existingRelation.get().getStatus();
            if (status == RelationStatus.ACCEPTED) throw new FriendException(FriendErrorCode.ALREADY_FRIEND);
            if (status == RelationStatus.REQUESTED) throw new FriendException(FriendErrorCode.ALREADY_REQUESTED);
            if (status == RelationStatus.BLOCKED) throw new FriendException(FriendErrorCode.ALREADY_BLOCKED);
        }

        //                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              친구 수 제한 검사
        if (friendRepository.countAcceptedFriends(myId) >= 100) {
            throw new FriendException(FriendErrorCode.FRIEND_LIMIT_EXCEEDED);
        }
        // 친구 요청 저장
        FriendRelation newRelation = FriendRelation.builder()
                .requesterId(myId)
                .receiverId(targetUser.getId())
                .status(RelationStatus.REQUESTED)
                .build();
        friendRepository.save(newRelation);
    }

    @Override
    public void acceptFriendRequest(Long myId, Long requestId) {
        FriendRelation relation = friendRepository.findById(requestId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.REQUEST_NOT_FOUND));

        if (!relation.getReceiverId().equals(myId) || relation.getStatus() != RelationStatus.REQUESTED) {
            throw new FriendException(FriendErrorCode.REQUEST_NOT_FOUND);
        }

        if (friendRepository.countAcceptedFriends(myId) >= 100 || friendRepository.countAcceptedFriends(relation.getRequesterId()) >= 100) {
            throw new FriendException(FriendErrorCode.FRIEND_LIMIT_EXCEEDED);
        }

        relation.accept();
        friendRepository.save(relation);
    }

    // 친구 요청 거절
    @Transactional
    public void rejectFriendRequest(Long myId, Long relationId) {
        FriendRelation relation = friendRepository.findById(relationId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (!relation.getReceiverId().equals(myId)) {
            throw new FriendException(FriendErrorCode.UNAUTHORIZED_ACTION);
        }

        // 거절 시 아예 삭제하는 정책이라면 deleteById 사용,
        // 거절 상태만 기록할 거라면 updateStatus(RelationStatus.REJECTED) 사용
        friendRepository.deleteById(relationId);
    }

    // 친구 삭제
    @Transactional
    public void deleteFriend(Long myId, Long relationId) {
        FriendRelation relation = friendRepository.findById(relationId)
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        // 둘 중 한 명이라도 내가 아니면 삭제 불가
        if (!relation.getRequesterId().equals(myId) && !relation.getReceiverId().equals(myId)) {
            throw new FriendException(FriendErrorCode.UNAUTHORIZED_ACTION);
        }

        // 친구 관계는 DB에서 완전히 지워버리는 것(Hard Delete)이 일반적입니다.
        friendRepository.delete(relation);
    }

    // 친구 차단
    @Transactional
    public void blockUser(Long myId, String targetUserCode) {
        User targetUser = userRepository.findByPersonalCode(targetUserCode)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        // 기존에 어떤 관계(친구, 대기중 등)라도 있는지 확인
        Optional<FriendRelation> existingRelation = friendRepository.findRelationBetween(myId, targetUser.getId());

        if (existingRelation.isPresent()) {
            FriendRelation relation = existingRelation.get();
            if (relation.getStatus() == RelationStatus.BLOCKED && relation.getRequesterId().equals(myId)) {
                throw new FriendException(FriendErrorCode.ALREADY_BLOCKED); // 이미 차단함
            }
            // 기존 관계를 싹 다 지우고 새 판을 짭니다.
            friendRepository.delete(relation);
        }

        // 차단 관계 새로 생성 (내가 요청자=차단자 가 됨)
        FriendRelation blockRelation = FriendRelation.builder()
                .requesterId(myId)
                .receiverId(targetUser.getId())
                .status(RelationStatus.BLOCKED)
                .build();

        friendRepository.save(blockRelation);
    }

    // 4. 차단 해제
    @Transactional
    public void unblockUser(Long myId, String targetUserCode) {
        User targetUser = userRepository.findByPersonalCode(targetUserCode)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        // 내가(requesterId) 쟤를(receiverId) 차단한 관계를 찾음
        FriendRelation relation = friendRepository.findByRequesterIdAndReceiverId(myId, targetUser.getId())
                .orElseThrow(() -> new FriendException(FriendErrorCode.RELATION_NOT_FOUND));

        if (relation.getStatus() != RelationStatus.BLOCKED) {
            throw new FriendException(FriendErrorCode.INVALID_STATUS); // 차단된 상태가 아님
        }

        // 차단 관계 삭제 (이제 다시 남남이 됨)
        friendRepository.delete(relation);
    }
}