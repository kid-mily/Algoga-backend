package com.kidmily.algoga_server.friend.presentation.api.response;
import com.kidmily.algoga_server.user.domain.User;
import lombok.Builder;
@Builder
public record FriendResponse(
        Long relationId, // 친구 관계(요청) 자체의 ID
        Long userId,
        String nickname,
        String personalCode,
        String profileImageUrl
) {
    public static FriendResponse of(Long relationId, User user) {
        return FriendResponse.builder()
                .relationId(relationId)
                .userId(user.getId())
                .nickname(user.getNickname())
                .personalCode(user.getPersonalCode())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}