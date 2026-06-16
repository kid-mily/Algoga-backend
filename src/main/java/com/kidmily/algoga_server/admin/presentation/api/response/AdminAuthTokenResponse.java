package com.kidmily.algoga_server.admin.presentation.api.response;

import com.kidmily.algoga_server.admin.domain.model.ManagerRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 토큰 및 상태 응답")
public record AdminAuthTokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "매니저 권한", example = "SUPER_ADMIN")
        ManagerRole role  // 🌟 추가
) {}