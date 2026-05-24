package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "일반 회원가입 요청")
public record AuthSignupRequest(
        @Schema(description = "이메일(아이디)", example = "test@algoga.com")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "비밀번호 (8자 이상, 영문+숫자 포함)", example = "password123")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문, 숫자 조합 8자 이상이어야 합니다.")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @Schema(description = "이름", example = "김알고")
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

        @Schema(description = "추천인 코드 (선택)", example = "REF123")
        String referralCode,

        @Schema(description = "유입 경로 (선택)", example = "인스타그램")
        String signupPath
) {}