package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증번호 발송 요청")
public record SendEmailCodeRequest(
        @Schema(description = "인증번호를 받을 이메일", example = "test@algoga.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        String email
) {}