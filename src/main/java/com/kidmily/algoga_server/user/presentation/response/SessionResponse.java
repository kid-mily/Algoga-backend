package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "로그인 세션 만료 정보 응답")
public record SessionResponse(
        @Schema(description = "AccessToken 만료 시각", example = "2026-07-08T07:00:00Z")
        Instant expiresAt,

        @Schema(description = "AccessToken 남은 유효 시간(초)", example = "1800")
        long remainingSeconds
) {}
