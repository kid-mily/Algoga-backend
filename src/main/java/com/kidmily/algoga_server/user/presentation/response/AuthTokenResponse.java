package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 토큰 및 상태 응답")
public record AuthTokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "비밀번호 변경 필요 여부 (임시 비밀번호로 로그인한 경우 true)", example = "false")
        boolean requiresPasswordChange
) {}