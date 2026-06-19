package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase.FriendView;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class
FriendQueryService implements FriendQueryUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public List<FriendView> getFriends(Long myId) {
        List<FriendRelation> relations = friendRepository.findAcceptedFriends(myId);

        return relations.stream().map(relation -> {
                    Long friendUserId = relation.getRequesterId().equals(myId) ? relation.getReceiverId() : relation.getRequesterId();
                    User friend = userRepository.findById(friendUserId).orElseThrow();

                    return new FriendView(
                            relation.getId(),
                            friend.getId(),
                            friend.getNickname(),
                            friend.getPersonalCode(),
                            friend.getProfileImageUrl()
                    );
                })
                .sorted((a, b) -> a.nickname().compareToIgnoreCase(b.nickname()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendView> getReceivedRequests(Long myId) {
        List<FriendRelation> requests = friendRepository.findByReceiverIdAndStatus(myId, RelationStatus.REQUESTED);

        return requests.stream().map(req -> {
            User requester = userRepository.findById(req.getRequesterId()).orElseThrow();
            return new FriendView(
                    req.getId(),
                    requester.getId(),
                    requester.getNickname(),
                    requester.getPersonalCode(),
                    requester.getProfileImageUrl()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public FriendView searchUserByCode(String code) {
        User user = userRepository.findByPersonalCode(code)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        return new FriendView(
                null,
                user.getId(),
                user.getNickname(),
                user.getPersonalCode(),
                user.getProfileImageUrl()
        );
    }

    @Override
    public long countFriends(Long userId) {
        // 친구 레포지토리에서 바로 카운트
        return friendRepository.countAcceptedFriends(userId);
    }

    @Override
    public List<com.kidmily.algoga_server.user.presentation.response.AdminFriendDetailResponse> getAdminFriendDetails(Long userId) {

        // 1. 수락 완료된 친구 관계 리스트 가져오기
        List<FriendRelation> relations = friendRepository.findAcceptedFriends(userId);

        // 2. 친구들의 User ID 추출
        List<Long> friendIds = relations.stream()
                .map(rel -> rel.getRequesterId().equals(userId) ? rel.getReceiverId() : rel.getRequesterId())
                .toList();

        // 3. (옵션) 최적화를 위해 UserRepository에서 닉네임만 맵으로 가져오는 로직
        // (이 부분을 위해 FriendQueryService 상단에 UserRepository 주입이 필요합니다)
        java.util.Map<Long, String> friendNicknameMap = userRepository.findAllById(friendIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, User::getNickname));

        // 4. 스키마 규격으로 매핑
        return relations.stream().map(rel -> {
            Long friendId = rel.getRequesterId().equals(userId) ? rel.getReceiverId() : rel.getRequesterId();
            String nickname = friendNicknameMap.getOrDefault(friendId, "알 수 없음(탈퇴유저)");

            return com.kidmily.algoga_server.user.presentation.response.AdminFriendDetailResponse.of(
                    friendId,
                    nickname,
                    rel.getUpdatedAt()
            );
        }).toList();
    }
}