package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 토큰 응답")
public record AuthTokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {}