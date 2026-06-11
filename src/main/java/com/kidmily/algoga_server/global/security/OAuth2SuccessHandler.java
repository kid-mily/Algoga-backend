package com.kidmily.algoga_server.global.security;

import com.kidmily.algoga_server.global.security.port.SocialLoginProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 도메인(AuthService)에 비즈니스 처리를 위임하기 위한 인터페이스 (클린 아키텍처 준수)
    private final SocialLoginProcessor socialLoginProcessor;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. UserService 에서 넘겨준 정보 꺼내기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 2. 유저 도메인 측에 토큰 발급 및 가입 여부 확인 로직을 위임하고, 이동할 URL만 받아옴
        String targetUrl = socialLoginProcessor.processLoginAndGetRedirectUrl(email, name);

        log.info("소셜 로그인 처리 완료. 다음 경로로 이동합니다: {}", targetUrl);

        // 3. 프론트엔드로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}