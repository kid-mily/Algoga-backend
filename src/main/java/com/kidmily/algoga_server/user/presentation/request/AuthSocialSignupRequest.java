package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "소셜 추가정보 회원가입 요청 (아이디 제외)")
public record AuthSocialSignupRequest(

        @Schema(description = "이메일 (구글/카카오에서 제공받은 읽기 전용 값)", example = "test_social@algoga.com")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "이름 (구글/카카오에서 제공받은 읽기 전용 값)", example = "김알고")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @Schema(description = "생년월일", example = "2000-01-01")
        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @Schema(description = "성별 (MALE/FEMALE/OTHER)", example = "MALE")
        @NotBlank(message = "성별은 필수입니다.")
        String gender,

        @Schema(description = "닉네임", example = "알고가조아")
        @Size(max = 50, message = "닉네임은 50자 이내여야 합니다.")
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @Schema(description = "소셜 타입 (예: GOOGLE, KAKAO)", example = "GOOGLE")
        @NotBlank(message = "소셜 타입은 필수입니다.")
        String socialType,

        @Schema(description = "추천인 코드 (선택)", example = "REF123")
        String referralCode,

        @Schema(description = "유입 경로 (선택)", example = "인스타그램")
        String signupPath,

        // 필수/선택 약관
        @Schema(description = "이용약관 동의 (필수)", example = "true")
        @NotNull(message = "이용약관 동의는 필수입니다.")
        @AssertTrue(message = "이용약관에 동의하셔야 합니다.")
        Boolean termsServiceAgreed,

        @Schema(description = "개인정보 처리방침 동의 (필수)", example = "true")
        @NotNull(message = "개인정보 처리방침 동의는 필수입니다.")
        @AssertTrue(message = "개인정보 처리방침에 동의하셔야 합니다.")
        Boolean termsPrivacyAgreed,

        @Schema(description = "마케팅 수신 동의 (선택)", example = "false")
        @NotNull(message = "마케팅 수신 동의 여부는 필수입니다.")
        Boolean termsMarketingAgreed
) {}