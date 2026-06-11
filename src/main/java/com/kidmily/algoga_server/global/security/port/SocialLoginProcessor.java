package com.kidmily.algoga_server.global.security.port;

public interface SocialLoginProcessor {
    /**
     * 소셜 로그인 성공 시 유저 정보를 바탕으로 DB 조회, JWT 발급 등을 수행하고
     * 프론트엔드로 리다이렉트할 최종 URL을 반환합니다.
     */
    String processLoginAndGetRedirectUrl(String email, String name);
}