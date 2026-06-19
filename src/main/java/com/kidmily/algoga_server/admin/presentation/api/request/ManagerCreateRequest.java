package com.kidmily.algoga_server.admin.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "매니저 생성 요청 (SUPER_ADMIN 전용)")
public record ManagerCreateRequest(
        @Schema(description = "로그인 아이디", example = "admin_01")
        @NotBlank(message = "아이디는 필수입니다.")
        String loginId,

        @Schema(description = "비밀번호 (8자 이상, 영문+숫자 포함)", example = "password123")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+~\\-=]{8,}$", message = "비밀번호는 영문, 숫자 조합(특수문자 포함 가능) 8자 이상이어야 합니다.")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @Schema(description = "이름", example = "홍길동 관리자")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
        String phone,

        @Schema(description = "이메일", example = "admin@algoga.com")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        String email,

        @Schema(description = "관리자 권한 (CONTENT_MANAGER, CS_MANAGER, SETTLEMENT_MANAGER, STATISTS_MANAGER, SUPER_ADMIN)", example = "CS_MANAGER")
        @NotBlank(message = "권한은 필수입니다.")
        String role
) {}