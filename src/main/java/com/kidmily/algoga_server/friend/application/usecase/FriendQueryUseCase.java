package com.kidmily.algoga_server.friend.application.usecase;

import lombok.Builder;

import java.util.List;
import java.util.Map;

public interface FriendQueryUseCase {
    List<FriendView> getFriends(Long myId);
    List<FriendView> getReceivedRequests(Long myId);
    List<FriendView> getBlockedUsers(Long myId);
    FriendView searchUserByCode(Long myId, String code);

    // 친구 유저 ID 목록만 필요한 소비처용 (예: 온라인 상태 브로드캐스트) — 닉네임/프로필 등 부가 조회 없이 가볍게 반환
    List<Long> getFriendUserIds(Long myId);

    // 관리자 페이지 리스트용 친구 수 카운트
    long countFriends(Long userId);

    // 관리자 유저 목록(페이지 단위)처럼 여러 유저의 친구 수를 한 번에 조회해야 할 때.
    // countFriends()를 유저 수만큼 반복 호출하는 N+1을 피하기 위한 배치 버전.
    Map<Long, Long> countFriendsForUsers(List<Long> userIds);

    // 관리자 페이지 상세조회용 친구 탭 목록 반환
    List<com.kidmily.algoga_server.user.presentation.response.AdminFriendDetailResponse> getAdminFriendDetails(Long userId);

    // DTO 폴더를 만들지 않고 UseCase 내부에 View 객체 선언
    // 연속된 boolean 필드가 여러 개라 위치 인자 생성자만 쓰면 순서 실수를 컴파일러가 못 잡아주므로 @Builder를 붙임
    @Builder
    record FriendView(
            Long relationId,
            Long userId,
            String nickname,
            String personalCode,
            String profileImageUrl,
            boolean isFavorite,
            boolean isOnline,
            boolean requestAvailable,
            String unavailableMessage
    ) {}
}