package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.user.application.AuthService;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.presentation.request.*;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.FindIdResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원 인증 API")
@RestController

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final GlobalJwtProvider globalJwtProvider;

    // 일반 회원가입
    @Operation(summary = "일반 회원가입", description = "모든 필수 항목 입력 및 유효성 검사를 거쳐 계정을 생성합니다.")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"ALREADY_EXISTS_EMAIL"})
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "약관 미동의 또는 입력값 유효성 검사 실패")
    })
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid AuthSignupRequest request) {
        authService.signup(request);

        return ApiResponse.created(
                "AUTH_SIGNUP_SUCCESS",
                "회원가입이 완료되었습니다.",
                null
        );
    }

    // 일반 로그인
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

    // 아이디 찾기
    @Operation(summary = "아이디 찾기", description = "이름과 이메일을 통해 마스킹된 아이디를 찾습니다.") // 문구 수정!
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER"})
    @PostMapping("/find-id")
    public ApiResponse<FindIdResponse> findId(@RequestBody @Valid FindIdRequest request) {
        FindIdResponse response = authService.findId(request);
        return ApiResponse.success("AUTH_FIND_ID_SUCCESS", "아이디 조회를 성공했습니다.", response);
    }

    // 비밀번호 찾기
    @Operation(summary = "비밀번호 찾기", description = "아이디와 이메일 일치 시 임시 비밀번호를 발급합니다.")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER"})
    @PostMapping("/find-password")
    public ApiResponse<Void> findPassword(@RequestBody @Valid FindPasswordRequest request) {
        authService.findPassword(request);
        return ApiResponse.success("AUTH_FIND_PW_SUCCESS", "임시 비밀번호가 이메일로 발송되었습니다.");
    }

    // 임시비번 발급 후 비밀번호 강제 변경
    @Operation(summary = "비밀번호 강제 변경", description = "임시 비밀번호로 로그인한 후, 새 비밀번호로 강제 변경합니다.")
    @PatchMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ResetPasswordRequest request) {

        // 토큰에서 추출한 이메일(username)과 변경할 비밀번호 정보를 넘깁니다.
        authService.resetPassword(userDetails.getUsername(), request);

        return ApiResponse.success("AUTH_RESET_PW_SUCCESS", "비밀번호 변경이 완료되었습니다. 다시 로그인해주세요.");
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "Redis에서 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        // 1. 헤더에서 토큰 추출 ("Bearer " 제거)
        String jwt = token.replace("Bearer ", "");

        // 2. 토큰에서 이메일 추출 (globalJwtProvider를 사용)
        String email = globalJwtProvider.getSubject(jwt);

        // 3. 서비스의 로그아웃 호출 (Redis 삭제 로직 실행)
        authService.logout(email);

        return ApiResponse.success("AUTH_LOGOUT_SUCCESS", "로그아웃이 완료되었습니다.");
    }

    // 이메일 인증번호 발송 API
    @Operation(summary = "회원가입 이메일 인증번호 발송", description = "입력한 이메일로 6자리 인증번호를 발송합니다.")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"ALREADY_EXISTS_EMAIL"})
    @PostMapping("/email/send-code")
    public ApiResponse<Void> sendEmailCode(@RequestBody @Valid SendEmailCodeRequest request) {
        authService.sendVerificationCode(request);
        return ApiResponse.success("AUTH_SEND_CODE_SUCCESS", "인증번호가 이메일로 발송되었습니다.");
    }

    // 이메일 인증번호 확인 API
    @Operation(summary = "회원가입 이메일 인증번호 확인", description = "발송된 6자리 인증번호가 맞는지 확인합니다.")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"INVALID_USER_INFO"})
    @PostMapping("/email/verify-code")
    public ApiResponse<Void> verifyEmailCode(@RequestBody @Valid VerifyEmailCodeRequest request) {
        authService.verifyEmailCode(request);
        return ApiResponse.success("AUTH_VERIFY_CODE_SUCCESS", "이메일 인증이 완료되었습니다.");
    }


}