package com.kidmily.algoga_server.admin.presentation.api;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.admin.application.usecase.ManagerAuthUseCase;
import com.kidmily.algoga_server.admin.exception.ManagerErrorCode;
import com.kidmily.algoga_server.admin.presentation.api.request.ManagerLoginRequest;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 🌟 추가
import org.springframework.web.bind.annotation.*;

@Slf4j // 🌟 추가
@Tag(name = "Admin Auth", description = "관리자 인증 (로그인) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
public class ManagerAuthController {

    private final ManagerAuthUseCase managerAuthUseCase;

    @Operation(summary = "매니저 로그인")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"MANAGER_NOT_FOUND", "DELETED_MANAGER", "INVALID_PASSWORD"})
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@RequestBody @Valid ManagerLoginRequest request) {

        log.info("[Manager Login] 관리자 로그인 시도 - ID: {}", request.loginId()); // 🌟 추가

        // DTO -> Command 변환 (프레젠테이션 계층의 책임)
        LoginManagerCommand command = new LoginManagerCommand(
                request.loginId(), request.password()
        );

        // 서비스(UseCase) 호출
        AuthTokenResponse response = managerAuthUseCase.login(command);

        log.info("[Manager Login] 관리자 로그인 성공 - ID: {}", request.loginId()); // 🌟 추가

        return ApiResponse.success(
                "MANAGER_LOGIN_SUCCESS",
                "관리자 로그인에 성공했습니다.",
                response
        );
    }
}