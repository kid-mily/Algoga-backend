package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 토큰 및 상태 응답 (실시간 프로필 동기화 정보 포함)")
public record AuthTokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "비밀번호 변경 필요 여부 (임시 비밀번호로 로그인한 경우 true)", example = "false")
        boolean requiresPasswordChange,

        @Schema(description = "최신 유저 닉네임", example = "알고가대장")
        String nickname,

        @Schema(description = "최신 프로필 이미지 URL (S3 경로)", example = "https://algoga-banner.s3.ap-northeast-2.amazonaws.com/profiles/avatar.png")
        String profileImageUrl
) {}