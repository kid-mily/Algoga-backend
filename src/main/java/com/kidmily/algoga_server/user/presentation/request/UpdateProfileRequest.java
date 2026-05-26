package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(
        @Schema(description = "닉네임", example = "알고가조아")
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "이메일", example = "new@algoga.com")
        String email
) {}