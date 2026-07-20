package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.user.application.AuthService;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.presentation.request.*;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.FindIdResponse;
import com.kidmily.algoga_server.user.presentation.response.SessionResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@Tag(name = "Auth", description = "회원 인증 API")
@RestController

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final GlobalJwtProvider globalJwtProvider;

    // 일반 회원가입
    @Operation(summary = "일반 회원가입", description = "모든 필수 항목 입력 및 유효성 검사를 거쳐 계정을 생성합니다.")
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"DUPLICATE_EMAIL", "RECENTLY_WITHDRAWN_EMAIL"})
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

    // 아이디 중복 확인 API
    @Operation(summary = "아이디 중복 확인", description = "회원가입 시 아이디(username)가 사용 가능한지 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "정상 처리 (data가 tr  ue면 사용 가능, false면 이미 사용 중인 중복 아이디)"
            )
    })
    @GetMapping("/username/check")
    public ApiResponse<Boolean> checkUsername(@RequestParam("username") String username) {
        boolean isAvailable = authService.isUsernameAvailable(username);

        if (isAvailable) {
            return ApiResponse.success(
                    "SUCCESS",
                    "사용 가능한 아이디입니다.",
                    true
            );
        } else {
            return ApiResponse.success(
                    "USERNAME_DUPLICATED",
                    "이미 사용 중인 아이디입니다.",
                    false
            );
        }
    }

    // 일반 로그인 (토큰을 HttpOnly 쿠키로 세팅)
    @Operation(summary = "일반 로그인")
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER", "DELETED_USER", "ACCOUNT_LOCKED", "INVALID_PASSWORD"})
    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody @Valid AuthLoginRequest request, HttpServletResponse response) {
        AuthTokenResponse tokenResponse = authService.login(request);

//        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
//                .httpOnly(true).secure(true).path("/").maxAge(30 * 60).sameSite("None").build();
//
//        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
//                .httpOnly(true).secure(true).path("/").maxAge(7 * 24 * 60 * 60).sameSite("None").build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
//        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        // 공통 메서드 호출 (깔끔!)
        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.createCookie("accessToken", tokenResponse.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.createCookie("refreshToken", tokenResponse.refreshToken()).toString());
        log.info("응답 헤더에 AccessToken / RefreshToken 쿠키 세팅 완료.");

        return ApiResponse.success("AUTH_LOGIN_SUCCESS", "로그인에 성공했습니다.", null);
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
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"PASSWORD_SAME_AS_OLD"})
    @PatchMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ResetPasswordRequest request) {

        // 토큰에서 추출한 이메일(username)과 변경할 비밀번호 정보를 넘깁니다.
        authService.resetPassword(userDetails.getUsername(), request);

        return ApiResponse.success("AUTH_RESET_PW_SUCCESS", "비밀번호 변경이 완료되었습니다. 다시 로그인해주세요.");
    }

    // 로그아웃 (쿠키를 읽어서 만료시킴)
    @Operation(summary = "로그아웃", description = "쿠키를 삭제하고 Redis에서 Refresh Token을 지웁니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken != null && globalJwtProvider.validateToken(accessToken)) {
            String email = globalJwtProvider.getSubject(accessToken);
            authService.logout(email);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.deleteCookie("accessToken").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.deleteCookie("refreshToken").toString());

        return ApiResponse.success("AUTH_LOGOUT_SUCCESS", "로그아웃이 완료되었습니다.");
    }

    // 이메일 인증번호 발송 API
    @Operation(summary = "회원가입 이메일 인증번호 발송", description = "입력한 이메일로 6자리 인증번호를 발송합니다.")
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"DUPLICATE_EMAIL", "RECENTLY_WITHDRAWN_EMAIL"})
    @PostMapping("/email/send-code")
    public ApiResponse<Void> sendEmailCode(@RequestBody @Valid SendEmailCodeRequest request) {
        authService.sendVerificationCode(request);
        return ApiResponse.success("AUTH_SEND_CODE_SUCCESS", "인증번호가 이메일로 발송되었습니다.");
    }

    // 이메일 인증번호 확인 API
    @Operation(summary = "회원가입 이메일 인증번호 확인", description = "발송된 6자리 인증번호가 맞는지 확인합니다.")
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"EMAIL_AUTH_CODE_MISMATCH"})
    @PostMapping("/email/verify-code")
    public ApiResponse<Void> verifyEmailCode(@RequestBody @Valid VerifyEmailCodeRequest request) {
        authService.verifyEmailCode(request);
        return ApiResponse.success("AUTH_VERIFY_CODE_SUCCESS", "이메일 인증이 완료되었습니다.");
    }

    // 소셜 추가정보 회원가입 API
    @Operation(summary = "소셜 추가정보 회원가입", description = "소셜 로그인 성공 후 최초 가입 시, 필수 추가 정보(전화번호, 성별, 닉네임 등)를 입력받아 회원가입을 완료합니다.")
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"DUPLICATE_EMAIL", "RECENTLY_WITHDRAWN_EMAIL"})
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"ALREADY_EXISTS_PHONE"})
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "소셜 회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패 또는 필수값 누락")
    })
    @PostMapping("/social/signup")
    public ResponseEntity<Void> socialSignup(@Valid @RequestBody AuthSocialSignupRequest request) {
        authService.socialSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 토큰 재발급 API (이것도 응답은 쿠키로 줌)
    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 수명이 남아있는 Refresh Token 쿠키를 이용해 자동으로 연장(재발급)합니다.")
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"REFRESH_TOKEN_NOT_FOUND", "SESSION_IDLE_TIMEOUT"}) // 유효하지 않거나 없을 때, 30분 미활동으로 만료됐을 때 에러 명시
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // 1. 브라우저가 보낸 쿠키들 중에서 refreshToken을 찾습니다.
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 2. 토큰에서 이메일을 뽑아냅니다.
        String email = globalJwtProvider.getSubject(refreshToken);

        // 3. 서비스에 가서 새 엑세스 토큰을 발급받아옵니다.
        String newAccessToken = authService.refreshAccessToken(email, refreshToken);

        // 공통 메서드 호출
        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.createCookie("accessToken", newAccessToken).toString());

        return ApiResponse.success("AUTH_REFRESH_SUCCESS", "토큰이 성공적으로 재발급되었습니다.", null);
    }

    // 로그인 세션 만료 정보 조회 (프론트 카운트다운 / 세션 연장 버튼용)
    @Operation(summary = "로그인 세션 만료 정보 조회", description = "AccessToken 쿠키를 읽어 만료 시각과 남은 유효 시간(초)을 반환합니다.")
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"INVALID_TOKEN"})
    @GetMapping("/session")
    public ApiResponse<SessionResponse> getSession(HttpServletRequest request) {
        String accessToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null || !globalJwtProvider.validateToken(accessToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        Date expiration = globalJwtProvider.getExpiration(accessToken);
        long remainingSeconds = Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);

        return ApiResponse.success(
                "AUTH_SESSION_INFO",
                "세션 만료 정보를 조회했습니다.",
                new SessionResponse(expiration.toInstant(), remainingSeconds)
        );
    }

}