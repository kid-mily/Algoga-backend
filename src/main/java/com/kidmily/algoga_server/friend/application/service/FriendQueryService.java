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
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendQueryService implements FriendQueryUseCase {

    private static final String ONLINE_KEY_PREFIX = "ONLINE:";

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 여러 유저의 온라인 여부를 조회한다.
    //
    // 주의: ElastiCache Serverless(및 클러스터 모드)에서는 MGET처럼 여러 키를 한 번에 다루는 "멀티키 명령"은
    // 모든 키가 같은 해시 슬롯에 있어야 하고, 아니면 CROSSSLOT 에러가 난다. ONLINE:{id} 키들은 슬롯이 흩어지므로
    // multiGet(MGET)은 여전히 쓸 수 없다.
    // 대신 단일 키 GET들을 executePipelined로 하나의 배치에 실어 보낸다. 파이프라이닝은 "명령들의 배치 전송"이지
    // MGET 같은 단일 멀티키 명령이 아니라서 슬롯 제약을 어기지 않는다(Lettuce 클러스터 커넥션이 각 명령을 알맞은
    // 노드로 라우팅). 그 결과 친구 수만큼 반복되던 Redis 왕복이 1회로 줄어든다.
    // 온라인 여부는 부가 정보이므로, Redis 장애 시에는 전체를 오프라인으로 간주하고 친구 목록 자체는 그대로 반환한다.
    private Set<Long> findOnlineUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Set.of();
        }

        try {
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                StringRedisConnection stringConnection = new DefaultStringRedisConnection(connection);
                for (Long id : userIds) {
                    stringConnection.get(ONLINE_KEY_PREFIX + id);
                }
                return null; // 파이프라인 모드에서는 콜백의 반환값이 쓰이지 않고, 결과는 executePipelined의 반환값으로 모아진다.
            });

            Set<Long> onlineUserIds = new HashSet<>();
            for (int i = 0; i < userIds.size(); i++) {
                if ("true".equals(results.get(i))) {
                    onlineUserIds.add(userIds.get(i));
                }
            }
            return onlineUserIds;
        } catch (Exception e) {
            log.warn("[FriendQueryService] 온라인 상태 배치 조회 실패, cause: {}", e.toString());
            return Set.of();
        }
    }

    @Override
    public List<Long> getFriendUserIds(Long myId) {
        return friendRepository.findAcceptedFriends(myId).stream()
                .map(rel -> rel.getRequesterId().equals(myId) ? rel.getReceiverId() : rel.getRequesterId())
                .distinct()
                .toList();
    }

    // getFriends/getReceivedRequests/getBlockedUsers가 공유하던 "관계 조회 -> 상대방 ID 추출 -> User 배치조회
    // -> 온라인 상태 배치조회 -> FriendView 조립" 4단계를 한 곳으로 모은 헬퍼.
    // counterpartIdExtractor로 관계마다 "나 아닌 상대방"의 ID를 어떻게 뽑을지만 다르게 넘기면 된다.
    // relation.isFavorite()는 ACCEPTED 상태가 아니면 항상 false로 저장되므로, 요청/차단 목록에서도 그대로 써도 무방하다.
    private List<FriendView> buildFriendViews(
            List<FriendRelation> relations,
            Function<FriendRelation, Long> counterpartIdExtractor
    ) {
        if (relations.isEmpty()) {
            return List.of();
        }

        // LinkedHashMap으로 원본 조회 순서를 유지 (HashMap을 쓰면 응답 순서가 뒤섞일 수 있음)
        Map<Long, FriendRelation> relationByCounterpartId = relations.stream()
                .collect(Collectors.toMap(
                        counterpartIdExtractor,
                        rel -> rel,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        List<Long> counterpartIds = List.copyOf(relationByCounterpartId.keySet());

        // User 엔티티들을 IN 쿼리로 한 번에 가져오기
        Map<Long, User> userById = userRepository.findAllById(counterpartIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 온라인 상태도 배치로 한 번에 조회
        Set<Long> onlineUserIds = findOnlineUserIds(counterpartIds);

        // 탈퇴 후 하드 삭제된 유저는 조용히 건너뜀 (없는 유저 조회로 500 나는 것 방지)
        return counterpartIds.stream()
                .filter(userById::containsKey)
                .map(id -> {
                    User user = userById.get(id);
                    FriendRelation relation = relationByCounterpartId.get(id);
                    return FriendView.builder()
                            .relationId(relation.getId())
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .personalCode(user.getPersonalCode())
                            .profileImageUrl(user.getProfileImageUrl())
                            .isFavorite(relation.isFavorite())
                            .isOnline(onlineUserIds.contains(user.getId()))
                            .requestAvailable(true) // 검색 결과 전용 필드라 이 컨텍스트에선 해당 없음
                            .unavailableMessage(null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendView> getFriends(Long myId) {
        List<FriendRelation> relations = friendRepository.findAcceptedFriends(myId);

        return buildFriendViews(relations, rel -> rel.getRequesterId().equals(myId) ? rel.getReceiverId() : rel.getRequesterId())
                .stream()
                // 닉네임이 null인 유저 데이터가 섞여 있어도 500이 나지 않도록 null을 맨 뒤로 보냄
                .sorted(Comparator.comparing(FriendView::nickname, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendView> getReceivedRequests(Long myId) {
        List<FriendRelation> requests = friendRepository.findByReceiverIdAndStatus(myId, RelationStatus.REQUESTED);
        return buildFriendViews(requests, FriendRelation::getRequesterId);
    }

    @Override
    public List<FriendView> getBlockedUsers(Long myId) {
        // 차단 관계는 requesterId=차단한 사람(나), receiverId=차단당한 사람으로 저장됨
        List<FriendRelation> blocks = friendRepository.findByRequesterIdAndStatus(myId, RelationStatus.BLOCKED);
        return buildFriendViews(blocks, FriendRelation::getReceiverId);
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

        // 다른 조회 메서드들과 동일하게 findOnlineUserIds()로 통일 (Redis 장애 시 예외를 삼키는 방어 로직도 동일하게 적용됨)
        boolean isOnline = findOnlineUserIds(List.of(user.getId())).contains(user.getId());
        String unavailableMessage = resolveUnavailableMessage(myId, user.getId(), relation);

        return FriendView.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .personalCode(user.getPersonalCode())
                .profileImageUrl(user.getProfileImageUrl())
                .isFavorite(false) // 검색 결과라 즐겨찾기 개념 없음
                .isOnline(isOnline)
                .requestAvailable(unavailableMessage == null)
                .unavailableMessage(unavailableMessage)
                .build();
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
    public Map<Long, Long> countFriendsForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        // countFriends()를 유저마다 반복 호출(N+1)하는 대신, 이 유저들과 관련된 ACCEPTED 관계를
        // 배치로 한 번에 가져와서 각자 몇 명인지 자바에서 센다.
        List<FriendRelation> relations = friendRepository.findAcceptedFriendsAmong(userIds);

        Set<Long> idSet = new HashSet<>(userIds);
        Map<Long, Long> counts = new HashMap<>();
        for (Long id : userIds) {
            counts.put(id, 0L);
        }
        for (FriendRelation rel : relations) {
            if (idSet.contains(rel.getRequesterId())) {
                counts.merge(rel.getRequesterId(), 1L, Long::sum);
            }
            if (idSet.contains(rel.getReceiverId())) {
                counts.merge(rel.getReceiverId(), 1L, Long::sum);
            }
        }
        return counts;
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
        // Collectors.toMap은 값이 null이면 NullPointerException을 던지므로, 닉네임이 없는 유저(더미 데이터 등)가
        // 섞여 있어도 500이 나지 않도록 null을 대체 문구로 치환한다.
        java.util.Map<Long, String> friendNicknameMap = userRepository.findAllById(friendIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        User::getId,
                        u -> u.getNickname() != null ? u.getNickname() : "닉네임 없음"
                ));

        // 4. 스키마 규격으로 매핑
        return relations.stream().map(rel -> {
            Long friendId = rel.getRequesterId().equals(userId) ? rel.getReceiverId() : rel.getRequesterId();
            String nickname = friendNicknameMap.getOrDefault(friendId, "알 수 없음(탈퇴유저)");

            // updatedAt이 없는(예: auditing 적용 전에 만들어진) 오래된 관계는 createdAt으로 대체해서 보여준다.
            LocalDateTime addedAt = rel.getUpdatedAt() != null ? rel.getUpdatedAt() : rel.getCreatedAt();

            return com.kidmily.algoga_server.user.presentation.response.AdminFriendDetailResponse.of(
                    friendId,
                    nickname,
                    addedAt
            );
        }).toList();
    }
}