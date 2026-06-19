package com.kidmily.algoga_server.admin.presentation.api;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.admin.application.usecase.ManagerAuthUseCase;
import com.kidmily.algoga_server.admin.exception.ManagerErrorCode;
import com.kidmily.algoga_server.admin.exception.ManagerException;
import com.kidmily.algoga_server.admin.presentation.api.request.ManagerLoginRequest;
import com.kidmily.algoga_server.admin.presentation.api.response.AdminAuthTokenResponse;
import com.kidmily.algoga_server.admin.presentation.api.response.AdminLoginResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Admin Auth", description = "관리자 인증 (로그인) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/admin")
public class ManagerAuthController {

    private final ManagerAuthUseCase managerAuthUseCase;
    private final GlobalJwtProvider globalJwtProvider;

    @Operation(summary = "매니저 로그인", description = "어드민 계정으로 로그인하여 토큰 쿠키를 발급받습니다.")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"MANAGER_NOT_FOUND", "DELETED_MANAGER", "INVALID_PASSWORD"})
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(
            @RequestBody @Valid ManagerLoginRequest request,
            HttpServletResponse response
    ) {

        log.info("[Manager Login] 관리자 로그인 시도 - ID: {}", request.loginId());

        LoginManagerCommand command = new LoginManagerCommand(request.loginId(), request.password());
        AdminAuthTokenResponse tokenResponse = managerAuthUseCase.login(command);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 60)
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("[Manager Login] 관리자 로그인 성공 - ID: {}", request.loginId());

        AdminLoginResponse responseBody = new AdminLoginResponse(tokenResponse.role());
        return ApiResponse.success("MANAGER_LOGIN_SUCCESS", "관리자 로그인에 성공했습니다.", responseBody);
    }

    @Operation(summary = "매니저 로그아웃", description = "토큰 쿠키를 만료시키고 DB(Redis)의 Refresh Token을 폐기합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("[Manager Logout] 관리자 로그아웃 요청");

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
            String loginId = globalJwtProvider.getSubject(accessToken);
            managerAuthUseCase.logout(loginId);
        }

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("[Manager Logout] 관리자 로그아웃 성공 (쿠키 만료 완료)");

        return ApiResponse.success("MANAGER_LOGOUT_SUCCESS", "관리자 로그아웃에 성공했습니다.", null);
    }

    // 🌟 변경: 반환 타입을 ApiResponse<AdminLoginResponse>로 교체하여 로그인과 똑같이 맞춤
    @Operation(summary = "어드민 토큰 재발급", description = "만료된 Access Token을 수명이 남아있는 Refresh Token 쿠키를 이용해 자동으로 연장하고 최신 Role 정보를 반환합니다.")
    @PostMapping("/refresh")
    public ApiResponse<AdminLoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new ManagerException(ManagerErrorCode.ACCESS_DENIED);
        }

        String loginId = globalJwtProvider.getSubject(refreshToken);

        // 1. 서비스단 호출 (신규 Access Token 및 현재 매니저의 Role 획득)
        AdminAuthTokenResponse tokenResponse = managerAuthUseCase.refreshAccessToken(loginId, refreshToken);

        // 2. 새 Access Token을 쿠키에 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 60) // 30분
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        log.info("[Manager Refresh] 관리자 토큰 및 권한 재발급 완료 - ID: {}", loginId);

        // 3. 로그인 성공 응답과 일치하게 JSON 바디에 최신 role 값을 담아 응답 처리 🌟
        AdminLoginResponse responseBody = new AdminLoginResponse(tokenResponse.role());
        return ApiResponse.success("MANAGER_REFRESH_SUCCESS", "관리자 토큰이 성공적으로 재발급되었습니다.", responseBody);
    }
}