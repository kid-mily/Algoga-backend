package com.kidmily.algoga_server.admin.presentation.api;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.admin.application.usecase.ManagerAuthUseCase;
import com.kidmily.algoga_server.admin.exception.ManagerErrorCode;
import com.kidmily.algoga_server.admin.presentation.api.request.ManagerLoginRequest;
import com.kidmily.algoga_server.admin.presentation.api.response.AdminAuthTokenResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Admin Auth", description = "관리자 인증 (로그인) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/admin") // 🌟 어드민 로그인 주소 변경! (유저 로그인과 같은 Public 구역으로 이동)
public class ManagerAuthController {

    private final ManagerAuthUseCase managerAuthUseCase;

    @Operation(summary = "매니저 로그인", description = "어드민 계정으로 로그인하여 토큰을 발급받습니다.")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"MANAGER_NOT_FOUND", "DELETED_MANAGER", "INVALID_PASSWORD"})
    @PostMapping("/login") // 🌟 최종 엔드포인트: POST /api/v1/auth/admin/login
    public ApiResponse<AdminAuthTokenResponse> login(@RequestBody @Valid ManagerLoginRequest request) {

        log.info("[Manager Login] 관리자 로그인 시도 - ID: {}", request.loginId());

        LoginManagerCommand command = new LoginManagerCommand(request.loginId(), request.password());
        AdminAuthTokenResponse response = managerAuthUseCase.login(command);

        log.info("[Manager Login] 관리자 로그인 성공 - ID: {}", request.loginId());

        return ApiResponse.success("MANAGER_LOGIN_SUCCESS", "관리자 로그인에 성공했습니다.", response);
    }
}