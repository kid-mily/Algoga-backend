package com.kidmily.algoga_server.user.presentation.response;

import com.kidmily.algoga_server.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자용 회원 상세 정보 및 활동 목록 응답")
public record AdminUserDetailResponse(
        @Schema(description = "회원 고유 ID", example = "1")
        Long userId,

        @Schema(description = "회원 닉네임", example = "알고가매니아")
        String nickname,

        @Schema(description = "회원 이메일", example = "user1@test.com")
        String email,

        @Schema(description = "회원 가입 일시", example = "2026-06-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "현재 실시간 접속(활동) 여부", example = "true")
        boolean isOnline,

        @Schema(description = "회원의 친구 목록")
        List<AdminFriendDetailResponse> friends,

        @Schema(description = "🤝 타 팀원 담당: 회원이 작성한 게시글 목록 (임시 타입)")
        List<Object> posts,

        @Schema(description = "🤝 타 팀원 담당: 회원이 작성한 댓글 목록 (임시 타입)")
        List<Object> comments
) {
    public static AdminUserDetailResponse of(User user, boolean isOnline, List<AdminFriendDetailResponse> friends) {
        return new AdminUserDetailResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt(),
                isOnline,
                friends,
                List.of(), // 타 팀원 담당 영역
                List.of()  // 타 팀원 담당 영역
        );
    }
}