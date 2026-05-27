package com.kidmily.algoga_server.admin.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "매니저 로그인 요청")
public record ManagerLoginRequest(
        @Schema(description = "로그인 아이디", example = "admin_01")
        @NotBlank(message = "아이디는 필수입니다.")
        String loginId,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {}