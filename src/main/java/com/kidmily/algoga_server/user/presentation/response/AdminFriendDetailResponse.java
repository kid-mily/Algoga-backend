package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자용 회원의 친구 상세 정보 응답")
public record AdminFriendDetailResponse(
        @Schema(description = "친구의 회원 고유 ID", example = "5")
        Long friendId,

        @Schema(description = "친구의 닉네임", example = "수원불주먹")
        String friendNickname,

        @Schema(description = "친구 관계 성립(추가) 일시", example = "2026-06-15T14:30:00")
        LocalDateTime addedAt
) {
    /**
     * 🌟 UserService에서 호출할 때 사용하는 정적 팩토리 메서드 'of'
     */
    public static AdminFriendDetailResponse of(Long friendId, String friendNickname, LocalDateTime addedAt) {
        return new AdminFriendDetailResponse(friendId, friendNickname, addedAt);
    }
}