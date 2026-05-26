package com.kidmily.algoga_server.user.presentation.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 확인 요청")
public record VerifyPasswordRequest(
        @Schema(description = "현재 비밀번호", example = "password123")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}