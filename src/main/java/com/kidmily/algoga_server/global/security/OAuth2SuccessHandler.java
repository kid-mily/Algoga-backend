package com.kidmily.algoga_server.global.security;

import com.kidmily.algoga_server.global.security.dto.SocialAuthResult;
import com.kidmily.algoga_server.global.security.port.SocialLoginProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SocialLoginProcessor socialLoginProcessor;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        SocialAuthResult authResult = socialLoginProcessor.processLoginAndGetRedirectUrl(email, name);

        if (authResult.accessToken() != null && authResult.refreshToken() != null) {
            // 🌟 클라우드(HTTPS) 서버와 로컬(HTTP) 간의 쿠키 전송을 위한 설정
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResult.accessToken())
                    .httpOnly(true)
                    .secure(true) // 🚨 무조건 true! (SameSite=None의 필수 조건)
                    .path("/")
                    .maxAge(30 * 60)
                    .sameSite("None") // 🚨 크로스 도메인 통신을 허용
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResult.refreshToken())
                    .httpOnly(true)
                    .secure(true) // 🚨 무조건 true!
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("None") // 🚨 크로스 도메인 허용
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        log.info("소셜 로그인 처리 완료. 다음 경로로 이동합니다: {}", authResult.redirectUrl());

        getRedirectStrategy().sendRedirect(request, response, authResult.redirectUrl());
    }
}