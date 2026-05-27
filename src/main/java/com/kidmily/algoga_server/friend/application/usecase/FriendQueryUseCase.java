package com.kidmily.algoga_server.friend.application.usecase;

import java.util.List;

public interface FriendQueryUseCase {
    List<FriendView> getFriends(Long myId);
    List<FriendView> getReceivedRequests(Long myId);
    FriendView searchUserByCode(String code);

    // 강사님 패턴: DTO 폴더를 만들지 않고 UseCase 내부에 View 객체 선언
    record FriendView(
            Long relationId,
            Long userId,
            String nickname,
            String personalCode,
            String profileImageUrl
    ) {}
}