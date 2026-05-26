package com.kidmily.algoga_server.user.presentation.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 변경 요청")
public record UpdatePasswordRequest(
        @Schema(description = "현재 비밀번호", example = "password123")
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자 포함)", example = "newPassword123!")
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 최소 8자 이상, 영문과 숫자를 포함해야 합니다.")
        String newPassword
) {}