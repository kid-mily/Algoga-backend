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
import jakarta.servlet.http.HttpServletResponse; // 🌟 추가
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;     // 🌟 추가
import org.springframework.http.ResponseCookie;  // 🌟 추가
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Admin Auth", description = "관리자 인증 (로그인) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/admin")
public class ManagerAuthController {

    private final ManagerAuthUseCase managerAuthUseCase;

    @Operation(summary = "매니저 로그인", description = "어드민 계정으로 로그인하여 토큰을 발급받습니다.")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"MANAGER_NOT_FOUND", "DELETED_MANAGER", "INVALID_PASSWORD"})
    @PostMapping("/login")
    public ApiResponse<AdminAuthTokenResponse> login( // 🌟 반환 타입을 ApiResponse<Void> 로 변경
                                    @RequestBody @Valid ManagerLoginRequest request,
                                    HttpServletResponse response // 🌟 쿠키 주입을 위해 추가
    ) {

        log.info("[Manager Login] 관리자 로그인 시도 - ID: {}", request.loginId());

        LoginManagerCommand command = new LoginManagerCommand(request.loginId(), request.password());
        AdminAuthTokenResponse tokenResponse = managerAuthUseCase.login(command);

        // 🌟 유저 도메인과 완전히 동일하게 쿠키 세팅 생성
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                .httpOnly(true)
                .secure(false) // HTTPS 운영 서버 배포 시 true로 변경
                .path("/")
                .maxAge(30 * 60) // 30분
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                .httpOnly(true)
                .secure(false) // HTTPS 운영 서버 배포 시 true로 변경
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("[Manager Login] 관리자 로그인 성공 - ID: {}", request.loginId());

        // 🌟 맨 마지막에 데이터를 응답 결과에 포함하지 않고 null로 반환하여 JSON에 출력되지 않게 합니다.
        return ApiResponse.success("MANAGER_LOGIN_SUCCESS", "관리자 로그인에 성공했습니다.", tokenResponse);
    }

    @Operation(summary = "매니저 로그아웃", description = "토큰 쿠키를 만료시켜 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            // @CurrentManager Long managerId, // (선택) 만약 Redis/DB에서 리프레시 토큰을 지워야 한다면 주석 해제 후 사용
            HttpServletResponse response
    ) {
        log.info("[Manager Logout] 관리자 로그아웃 요청");

        // 🌟 (선택) Redis나 DB에 저장된 Refresh Token을 폐기하는 로직이 있다면 여기서 UseCase를 호출하세요.
        // managerAuthUseCase.logout(managerId);

        // 🌟 1. Access Token 쿠키 즉시 만료 (maxAge = 0)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false) // HTTPS 운영 서버 배포 시 true로 변경
                .path("/")
                .maxAge(0) // 0으로 설정하여 브라우저에서 즉시 삭제되도록 유도
                .sameSite("None")
                .build();

        // 🌟 2. Refresh Token 쿠키 즉시 만료 (maxAge = 0)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // HTTPS 운영 서버 배포 시 true로 변경
                .path("/")
                .maxAge(0) // 0으로 설정하여 브라우저에서 즉시 삭제되도록 유도
                .sameSite("None")
                .build();

        // 🌟 3. 응답 헤더에 만료된 쿠키를 세팅하여 기존 쿠키 덮어쓰기
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("[Manager Logout] 관리자 로그아웃 성공 (쿠키 만료 완료)");

        return ApiResponse.success("MANAGER_LOGOUT_SUCCESS", "관리자 로그아웃에 성공했습니다.", null);
    }
}