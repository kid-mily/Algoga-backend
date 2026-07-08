package com.kidmily.algoga_server.global.security;

import com.kidmily.algoga_server.global.security.dto.SocialAuthResult;
import com.kidmily.algoga_server.global.security.port.SocialLoginProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
    private final GlobalJwtProvider globalJwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 🌟 1. 어떤 소셜(구글, 카카오)로 로그인했는지 추출!
        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
        String socialType = oauthToken.getAuthorizedClientRegistrationId().toUpperCase(); // "GOOGLE" 또는 "KAKAO"

        // 🌟 2. 추출한 socialType을 같이 넘겨줍니다.
        SocialAuthResult authResult = socialLoginProcessor.processLoginAndGetRedirectUrl(email, name, socialType);

        if (authResult.accessToken() != null && authResult.refreshToken() != null) {
            // 🌟 일반 로그인/로그아웃과 동일한 쿠키 속성(domain 포함)을 쓰도록 공통 메서드로 통일
            //    (domain이 다르면 브라우저가 별개의 쿠키로 취급해서, 로그아웃해도 이 쿠키가 안 지워지고
            //     이후 일반 로그인 쿠키와 함께 남아있다가 인증에 잘못 쓰이는 문제가 있었음)
            response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.createCookie("accessToken", authResult.accessToken()).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.createCookie("refreshToken", authResult.refreshToken()).toString());
        }

        log.info("소셜 로그인 처리 완료. 다음 경로로 이동합니다: {}", authResult.redirectUrl());

        getRedirectStrategy().sendRedirect(request, response, authResult.redirectUrl());
    }
}