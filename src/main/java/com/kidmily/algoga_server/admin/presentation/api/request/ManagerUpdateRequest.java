package com.kidmily.algoga_server.admin.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "매니저 수정 요청 (SUPER_ADMIN 전용)")
public record ManagerUpdateRequest(
        @Schema(description = "수정할 권한", example = "CONTENT_MANAGER")
        @NotBlank(message = "권한은 필수입니다.")
        String role,

        @Schema(description = "수정할 전화번호", example = "010-9999-8888")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
        String phone,

        @Schema(description = "수정할 이메일", example = "newadmin@algoga.com")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        String email
) {}