package com.kidmily.algoga_server.global.security.port;

import com.kidmily.algoga_server.global.security.dto.SocialAuthResult;

public interface SocialLoginProcessor {
    // 🌟 리다이렉트 주소와 두 가지 토큰(Access, Refresh)을 모두 담아 핸들러로 넘기기 위해
    // 반환 타입을 String에서 SocialAuthResult로 변경했습니다.
    SocialAuthResult processLoginAndGetRedirectUrl(String email, String name);
}