package com.kidmily.algoga_server.user.presentation.response;

import com.kidmily.algoga_server.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "유저 프로필 조회 응답")
public record UserProfileResponse(
        @Schema(description = "아이디", example = "test0606")
        String username,

        @Schema(description = "비밀번호 (마스킹 처리됨)", example = "********")
        String password,

        @Schema(description = "이름", example = "김알고")
        String name,

        @Schema(description = "닉네임", example = "알고가조아")
        String nickname,

        @Schema(description = "이메일", example = "test@algoga.com")
        String email,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "성별", example = "MALE")
        String gender,

        @Schema(description = "생년월일", example = "2000-01-01")
        String birthDate,

        @Schema(description = "개인 식별 번호(UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        String personalCode
) {
    // User 엔티티를 받아서 DTO로 변환하는 정적 팩토리 메서드
    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .username(user.getUsername())
                .password("********")
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .phone(user.getPhone())
                .gender(user.getGender().name())
                .birthDate(user.getBirthDate().toString())
                .personalCode(user.getPersonalCode())
                .build();
    }
}