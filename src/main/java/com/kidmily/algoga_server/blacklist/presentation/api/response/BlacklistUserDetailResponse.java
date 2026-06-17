package com.kidmily.algoga_server.blacklist.presentation.api.response;

import com.kidmily.algoga_server.blacklist.infrastructure.persistence.query.BlacklistUserQueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "블랙리스트 (후보 및 등록) 유저 상세 정보 응답 DTO")
public record BlacklistUserDetailResponse(
        @Schema(description = "유저 고유 PK", example = "105")
        Long userId,

        @Schema(description = "로그인 아이디", example = "baduser123")
        String username,

        @Schema(description = "유저 실명", example = "홍길동")
        String name,

        @Schema(description = "유저 닉네임", example = "악플러킬러")
        String nickname,

        @Schema(description = "유저 이메일", example = "baduser@example.com")
        String email,

        @Schema(description = "처리 완료된 누적 신고 횟수", example = "7")
        long reportCount,

        @Schema(description = "마지막 신고 접수/처리 일시", example = "2026-06-16T15:30:00")
        LocalDateTime lastReportedAt,

        @Schema(description = "현재 블랙리스트 등록 상태 여부 (true: 정지됨, false: 정상 이용중)", example = "false")
        boolean isBlacklisted
) {
    public static BlacklistUserDetailResponse from(BlacklistUserQueryProjection p) {
        return new BlacklistUserDetailResponse(
                p.getUserId(),
                p.getUsername(),
                p.getName(),
                p.getNickname(),
                p.getEmail(),
                p.getReportCount() != null ? p.getReportCount() : 0L,
                p.getLastReportedAt(),
                p.getIsBlacklisted() != null && p.getIsBlacklisted() > 0
        );
    }
}