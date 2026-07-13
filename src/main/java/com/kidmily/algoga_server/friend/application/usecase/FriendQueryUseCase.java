package com.kidmily.algoga_server.friend.application.usecase;

import java.util.List;

public interface FriendQueryUseCase {
    List<FriendView> getFriends(Long myId);
    List<FriendView> getReceivedRequests(Long myId);
    List<FriendView> getBlockedUsers(Long myId);
    FriendView searchUserByCode(String code);

    // 🌟 1. 추가: 관리자 페이지 리스트용 친구 수 카운트 기능
    long countFriends(Long userId);

    // 🌟 2. 추가: 관리자 페이지 상세조회용 친구 탭 목록 반환 (이전 UserService에서 옮겨올 로직)
    List<com.kidmily.algoga_server.user.presentation.response.AdminFriendDetailResponse> getAdminFriendDetails(Long userId);

    // 강사님 패턴: DTO 폴더를 만들지 않고 UseCase 내부에 View 객체 선언
    record FriendView(
            Long relationId,
            Long userId,
            String nickname,
            String personalCode,
            String profileImageUrl,
            boolean isFavorite,
            boolean isOnline
    ) {}
}