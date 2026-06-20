package com.kidmily.algoga_server.user.presentation.response;

import com.kidmily.algoga_server.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자용 전체 회원 목록 조회 응답")
public record AdminUserListResponse(
        @Schema(description = "회원 고유 ID", example = "1")
        Long userId,

        @Schema(description = "회원 닉네임", example = "알고가매니아")
        String nickname,

        @Schema(description = "회원 이메일", example = "user1@test.com")
        String email,

        @Schema(description = "회원 가입 일시", example = "2026-06-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "회원이 맺은 친구 수", example = "5")
        long friendCount,

        @Schema(description = "🤝 타 팀원 담당: 회원이 작성한 게시글 수", example = "12")
        long postCount,

        @Schema(description = "🤝 타 팀원 담당: 회원이 작성한 댓글 수", example = "34")
        long commentCount
) {
    /**
     * 🌟 UserService에서 호출할 때 사용하는 정적 팩토리 메서드 'of'
     * 매개변수: User 엔티티, 친구 수, 게시글 수, 댓글 수 순서대로 받습니다.
     */
    public static AdminUserListResponse of(User user, long friendCount, long postCount, long commentCount) {
        return new AdminUserListResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt(),
                friendCount,
                postCount,
                commentCount
        );
    }
}