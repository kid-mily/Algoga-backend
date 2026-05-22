package com.kidmily.algoga_server.user.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.command.AuthLoginCommand;
import com.kidmily.algoga_server.user.application.command.AuthSignupCommand;
import com.kidmily.algoga_server.user.application.usecase.AuthCommandUseCase;
import com.kidmily.algoga_server.user.domain.model.Gender;
import com.kidmily.algoga_server.user.presentation.api.request.AuthLoginRequest;
import com.kidmily.algoga_server.user.presentation.api.request.AuthSignupRequest;
import com.kidmily.algoga_server.user.presentation.api.response.AuthTokenResponse;
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

    private final AuthCommandUseCase authCommandUseCase;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid AuthSignupRequest request) {
        AuthSignupCommand command = new AuthSignupCommand(
                request.email(),
                request.password(),
                request.name(),
                request.phone(),
                request.birthDate(),
                Gender.valueOf(request.gender()),
                request.nickname()
        );
        authCommandUseCase.signup(command);
        return new ApiResponse<>(true, null, null); // 하연님 프로젝트 응답 폼에 맞춤
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@RequestBody @Valid AuthLoginRequest request) {
        AuthLoginCommand command = new AuthLoginCommand(
                request.email(),
                request.password()
        );
        AuthTokenResponse response = authCommandUseCase.login(command);
        return new ApiResponse<>(true, response, null);
    }
}