package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.AuthService;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.presentation.request.FindIdRequest;
import com.kidmily.algoga_server.user.presentation.request.AuthLoginRequest;
import com.kidmily.algoga_server.user.presentation.request.AuthSignupRequest;
import com.kidmily.algoga_server.user.presentation.request.FindPasswordRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.FindIdResponse;
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



    @Operation(summary = "아이디 찾기", description = "이름과 이메일을 통해 마스킹된 아이디를 찾습니다.") // 문구 수정!
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER"})
    @PostMapping("/find-id")
    public ApiResponse<FindIdResponse> findId(@RequestBody @Valid FindIdRequest request) {
        FindIdResponse response = authService.findId(request);
        return ApiResponse.success("AUTH_FIND_ID_SUCCESS", "아이디 조회를 성공했습니다.", response);
    }


    @Operation(summary = "비밀번호 찾기", description = "아이디와 이메일 일치 시 임시 비밀번호를 발급합니다.")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER"})
    @PostMapping("/find-password")
    public ApiResponse<Void> findPassword(@RequestBody @Valid FindPasswordRequest request) { // ⭐️ FindIdRequest -> FindPasswordRequest로 변경
        authService.findPassword(request);
        return ApiResponse.success("AUTH_FIND_PW_SUCCESS", "임시 비밀번호가 이메일로 발송되었습니다.");
    }
}