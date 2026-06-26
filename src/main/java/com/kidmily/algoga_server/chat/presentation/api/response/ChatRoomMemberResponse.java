package com.kidmily.algoga_server.chat.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 멤버 응답")
public record ChatRoomMemberResponse(

        @Schema(description = "유저 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl
) {
    public static ChatRoomMemberResponse of(Long userId, String nickname, String profileImageUrl) {
        return new ChatRoomMemberResponse(userId, nickname, profileImageUrl);
    }
}