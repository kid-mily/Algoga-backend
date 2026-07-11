package com.kidmily.algoga_server.friend.presentation.api.response;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase.FriendView;
import lombok.Builder;

@Builder
public record FriendResponse(
        Long relationId,
        Long userId,
        String nickname,
        String personalCode,
        String profileImageUrl,
        boolean isFavorite
) {
    // Application의 View 객체를 Presentation의 Response로 변환
    public static FriendResponse from(FriendView view) {
        return FriendResponse.builder()
                .relationId(view.relationId())
                .userId(view.userId())
                .nickname(view.nickname())
                .personalCode(view.personalCode())
                .profileImageUrl(view.profileImageUrl())
                .isFavorite(view.isFavorite())
                .build();
    }
}