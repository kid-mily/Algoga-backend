package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.AuthService;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.presentation.request.AuthLoginRequest;
import com.kidmily.algoga_server.user.presentation.request.AuthSignupRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "일반 회원가입")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"ALREADY_EXISTS_EMAIL"})
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid AuthSignupRequest request) {
        authService.signup(request);

        return ApiResponse.created(
                "AUTH_SIGNUP_SUCCESS",
                "회원가입이 완료되었습니다.",
                null
        );
    }

    @Operation(summary = "일반 로그인")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER", "DELETED_USER", "ACCOUNT_LOCKED", "INVALID_PASSWORD"})
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@RequestBody @Valid AuthLoginRequest request) {
        AuthTokenResponse response = authService.login(request);

        return ApiResponse.success(
                "AUTH_LOGIN_SUCCESS",
                "로그인에 성공했습니다.",
                response
        );
    }
}