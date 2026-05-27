package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
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
    public ApiResponse<Void> findPassword(@RequestBody @Valid FindPasswordRequest request) { // ⭐️ FindIdRequest -> FindPasswordRequest로 변경
        authService.findPassword(request);
        return ApiResponse.success("AUTH_FIND_PW_SUCCESS", "임시 비밀번호가 이메일로 발송되었습니다.");
    }

    // 임시비번 발급 후 비밀번호 강제 변경
    @Operation(summary = "비밀번호 강제 변경", description = "임시 비밀번호로 로그인한 후, 새 비밀번호로 강제 변경합니다.")
    @PatchMapping("/reset-password") // ⭐️ POST 대신 리소스를 부분 수정하는 PATCH 사용!
    public ApiResponse<Void> resetPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ResetPasswordRequest request) {

        // 토큰에서 추출한 이메일(username)과 변경할 비밀번호 정보를 넘깁니다.
        authService.resetPassword(userDetails.getUsername(), request);

        return ApiResponse.success("AUTH_RESET_PW_SUCCESS", "비밀번호 변경이 완료되었습니다. 다시 로그인해주세요.");
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "로그아웃 처리를 합니다. (클라이언트 단 토큰 삭제 필요)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 토큰을 통해 인증된 유저 정보가 들어옵니다.
        if (userDetails != null) {
            authService.logout(userDetails.getUsername()); // CustomUserDetails에서 반환하는 username은 email입니다.
        }
        return ApiResponse.success("AUTH_LOGOUT_SUCCESS", "로그아웃이 완료되었습니다.");
    }


}