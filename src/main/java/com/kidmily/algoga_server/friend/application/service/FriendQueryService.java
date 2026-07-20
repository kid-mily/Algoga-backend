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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class
FriendQueryService implements FriendQueryUseCase {

    private static final String ONLINE_KEY_PREFIX = "ONLINE:";

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 여러 유저의 온라인 여부를 조회한다.
    //
    // 주의: ElastiCache Serverless(및 클러스터 모드)에서는 MGET처럼 여러 키를 한 번에 다루는 명령은
    // 모든 키가 같은 해시 슬롯에 있어야 하고, 아니면 CROSSSLOT 에러가 난다.
    // ONLINE:{id} 키들은 슬롯이 흩어지므로 multiGet(MGET)을 쓰면 실패한다.
    // → 단일 키 GET(각각 단일 슬롯이라 클러스터 안전)을 반복해서 조회한다.
    // 또한 온라인 여부는 부가 정보이므로, Redis 장애 시에도 친구 목록 자체는 반환되도록 예외를 삼킨다.
    private Set<Long> findOnlineUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Set.of();
        }

        Set<Long> onlineUserIds = new HashSet<>();
        for (Long id : userIds) {
            try {
                String value = redisTemplate.opsForValue().get(ONLINE_KEY_PREFIX + id);
                if ("true".equals(value)) {
                    onlineUserIds.add(id);
                }
            } catch (Exception e) {
                // 온라인 정보 조회 실패는 목록 조회를 막지 않는다(부가 정보). 해당 유저는 오프라인으로 간주.
                log.warn("[FriendQueryService] 온라인 상태 조회 실패 - userId: {}, cause: {}", id, e.toString());
            }
        }
        return onlineUserIds;
    }

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

        // 3-1. 온라인 상태도 배치로 한 번에 조회
        Set<Long> onlineUserIds = findOnlineUserIds(friendIds);

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
                            relation.isFavorite(),
                            onlineUserIds.contains(friend.getId()),
                            true, // 검색 결과 전용 필드라 이 컨텍스트에선 해당 없음
                            null
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

        Set<Long> onlineUserIds = findOnlineUserIds(requesterIds);

        // 탈퇴 후 하드 삭제된 유저가 보낸 요청은 조회 목록에서 조용히 건너뜀 (없는 유저 조회로 500 나는 것 방지)
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
                            false, // 친구 요청 단계라 즐겨찾기 개념 없음
                            onlineUserIds.contains(requester.getId()),
                            true, // 검색 결과 전용 필드라 이 컨텍스트에선 해당 없음
                            null
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

        Set<Long> onlineUserIds = findOnlineUserIds(blockedUserIds);

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
                            false, // 차단 목록이라 즐겨찾기 개념 없음
                            onlineUserIds.contains(blockedUser.getId()),
                            true, // 검색 결과 전용 필드라 이 컨텍스트에선 해당 없음
                            null
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public FriendView searchUserByCode(Long myId, String code) {
        User user = userRepository.findByPersonalCode(code)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        Optional<FriendRelation> relation = friendRepository.findRelationBetween(myId, user.getId());

        if (relation.isPresent() && relation.get().getStatus() == RelationStatus.BLOCKED) {
            boolean blockedByMe = relation.get().getRequesterId().equals(myId);
            if (blockedByMe) {
                // 내가 차단한 상대: 검색 결과는 숨기되, 차단 사실을 잊었을 수 있으니 사유는 에러로 안내
                throw new FriendException(FriendErrorCode.ALREADY_BLOCKED);
            }
            // 상대가 나를 차단: 차단당한 사실 자체를 알려주지 않기 위해 "존재하지 않는 유저"와 동일하게 처리
            throw new FriendException(FriendErrorCode.USER_NOT_FOUND);
        }

        boolean isOnline = Boolean.TRUE.equals(redisTemplate.hasKey(ONLINE_KEY_PREFIX + user.getId()));
        String unavailableMessage = resolveUnavailableMessage(myId, user.getId(), relation);

        return new FriendView(
                null,
                user.getId(),
                user.getNickname(),
                user.getPersonalCode(),
                user.getProfileImageUrl(),
                false, // 검색 결과라 즐겨찾기 개념 없음
                isOnline,
                unavailableMessage == null,
                unavailableMessage
        );
    }

    // 친구 요청 전 미리보기용 판별 로직. sendFriendRequest()의 검증 순서/메시지를 그대로 재사용해서
    // 검색 화면에 뜨는 안내 문구가 실제 요청 시 나오는 에러 메시지와 항상 일치하도록 한다.
    // 차단(BLOCKED) 관계는 searchUserByCode()에서 이미 예외로 걸러지고 이 메서드까지 오지 않는다.
    private String resolveUnavailableMessage(Long myId, Long targetUserId, Optional<FriendRelation> relation) {
        if (targetUserId.equals(myId)) {
            return FriendErrorCode.CANNOT_ADD_SELF.getMessage();
        }

        return relation.map(r -> switch (r.getStatus()) {
                    case ACCEPTED -> FriendErrorCode.ALREADY_FRIEND.getMessage();
                    case REQUESTED -> FriendErrorCode.ALREADY_REQUESTED.getMessage();
                    case BLOCKED -> null; // 방어적 처리: 이 시점엔 이미 위에서 걸러졌어야 함
                    case REJECTED -> null;
                })
                .orElse(null);
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

        // 3.최적화를 위해 UserRepository에서 닉네임만 맵으로 가져오는 로직
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