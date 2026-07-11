package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase.FriendView;
import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
// import com.kidmily.algoga_server.friend.settings.cache.FriendCacheType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
        // 1. 수락된 친구 관계 목록 가져오기 (쿼리 1번)
        List<FriendRelation> relations = friendRepository.findAcceptedFriends(myId);

        if (relations.isEmpty()) {
            return List.of(); // 친구가 없으면 바로 리턴하여 쿼리 방지
        }

        // 2. 친구들의 ID만 리스트로 추출
        List<Long> friendIds = relations.stream()
                .map(rel -> rel.getRequesterId().equals(myId) ? rel.getReceiverId() : rel.getRequesterId())
                .toList();

        // 3. User 엔티티들을 IN 쿼리로 한 번에 싹 다 가져오기 (쿼리 1번) -> 총 쿼리 2번으로 끝!
        // JPA의 findAllById는 내부적으로 WHERE user_id IN (1, 2, 3...) 쿼리를 날립니다.
        List<User> friends = userRepository.findAllById(friendIds);

        // 4. 조립 및 정렬
        return friends.stream()
                .map(friend -> {
                    // relationId/즐겨찾기 여부가 필요하므로 relations 리스트에서 매칭시킴
                    FriendRelation relation = relations.stream()
                            .filter(r -> r.getRequesterId().equals(friend.getId()) || r.getReceiverId().equals(friend.getId()))
                            .findFirst().get();
                    return new FriendView(
                            relation.getId(),
                            friend.getId(),
                            friend.getNickname(),
                            friend.getPersonalCode(),
                            friend.getProfileImageUrl(),
                            relation.isFavorite()
                    );
                })
                .sorted((a, b) -> a.nickname().compareToIgnoreCase(b.nickname()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendView> getReceivedRequests(Long myId) {
        List<FriendRelation> requests = friendRepository.findByReceiverIdAndStatus(myId, RelationStatus.REQUESTED);

        if (requests.isEmpty()) {
            return List.of();
        }

        // 요청자들의 정보를 배치로 한 번에 조회 (N+1 방지)
        List<Long> requesterIds = requests.stream()
                .map(FriendRelation::getRequesterId)
                .distinct()
                .toList();

        Map<Long, User> requesterById = userRepository.findAllById(requesterIds).stream()
                .collect(Collectors.toMap(User::getId, requester -> requester));

        // 🌟 탈퇴 후 하드 삭제된 유저가 보낸 요청은 조회 목록에서 조용히 건너뜀 (없는 유저 조회로 500 나는 것 방지)
        return requests.stream()
                .filter(req -> requesterById.containsKey(req.getRequesterId()))
                .map(req -> {
                    User requester = requesterById.get(req.getRequesterId());
                    return new FriendView(
                            req.getId(),
                            requester.getId(),
                            requester.getNickname(),
                            requester.getPersonalCode(),
                            requester.getProfileImageUrl(),
                            false // 친구 요청 단계라 즐겨찾기 개념 없음
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public List<FriendView> getBlockedUsers(Long myId) {
        // 차단 관계는 requesterId=차단한 사람(나), receiverId=차단당한 사람으로 저장됨
        List<FriendRelation> blocks = friendRepository.findByRequesterIdAndStatus(myId, RelationStatus.BLOCKED);

        if (blocks.isEmpty()) {
            return List.of();
        }

        // 차단당한 유저들의 정보를 배치로 한 번에 조회 (N+1 방지)
        List<Long> blockedUserIds = blocks.stream()
                .map(FriendRelation::getReceiverId)
                .distinct()
                .toList();

        Map<Long, User> blockedUserById = userRepository.findAllById(blockedUserIds).stream()
                .collect(Collectors.toMap(User::getId, blockedUser -> blockedUser));

        // 탈퇴 후 하드 삭제된 유저에 대한 차단 기록은 조용히 건너뜀
        return blocks.stream()
                .filter(block -> blockedUserById.containsKey(block.getReceiverId()))
                .map(block -> {
                    User blockedUser = blockedUserById.get(block.getReceiverId());
                    return new FriendView(
                            block.getId(),
                            blockedUser.getId(),
                            blockedUser.getNickname(),
                            blockedUser.getPersonalCode(),
                            blockedUser.getProfileImageUrl(),
                            false // 차단 목록이라 즐겨찾기 개념 없음
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
                user.getProfileImageUrl(),
                false // 검색 결과라 즐겨찾기 개념 없음
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