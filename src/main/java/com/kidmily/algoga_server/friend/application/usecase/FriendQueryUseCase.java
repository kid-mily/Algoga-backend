package com.kidmily.algoga_server.friend.application.usecase;

import java.util.List;

public interface FriendQueryUseCase {
    List<FriendView> getFriends(Long myId);
    List<FriendView> getReceivedRequests(Long myId);
    List<FriendView> getBlockedUsers(Long myId);
    FriendView searchUserByCode(Long myId, String code);

    // 친구 유저 ID 목록만 필요한 소비처용 (예: 온라인 상태 브로드캐스트) — 닉네임/프로필 등 부가 조회 없이 가볍게 반환
    List<Long> getFriendUserIds(Long myId);

    // 관리자 페이지 리스트용 친구 수 카운트
    long countFriends(Long userId);

    // 관리자 페이지 상세조회용 친구 탭 목록 반환
    List<com.kidmily.algoga_server.user.presentation.response.AdminFriendDetailResponse> getAdminFriendDetails(Long userId);

    // DTO 폴더를 만들지 않고 UseCase 내부에 View 객체 선언
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