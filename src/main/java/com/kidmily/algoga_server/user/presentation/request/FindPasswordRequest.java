package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 찾기 요청")
public record FindPasswordRequest(
        @Schema(description = "아이디", example = "algoga123")
        @NotBlank(message = "아이디는 필수입니다.")
        String username, // 아이디

        @Schema(description = "가입 시 등록한 이메일", example = "test@algoga.com")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email
) {}