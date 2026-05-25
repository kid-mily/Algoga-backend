package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 강제 변경 요청 (임시 비밀번호 로그인 시)")
public record ResetPasswordRequest(
        @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자 포함)", example = "newPassword123")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문, 숫자 조합 8자 이상이어야 합니다.")
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        String newPassword
) {}