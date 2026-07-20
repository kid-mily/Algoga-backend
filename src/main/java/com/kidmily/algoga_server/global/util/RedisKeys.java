package com.kidmily.algoga_server.global.util;

// 여러 클래스(AuthService, UserService, UserEventListener, GlobalJwtAuthenticationFilter)에서
// 문자열 리터럴로 흩어져 있던 Redis 키 접두사를 한 곳에 모아, 오타로 인한 read/write 키 불일치를 방지한다.
public final class RedisKeys {

    public static final String REFRESH_TOKEN_PREFIX = "RT:";
    public static final String ACTIVE_AT_PREFIX = "ACTIVE_AT:";
    public static final String BLACKLIST_PREFIX = "BLACKLIST:";
    public static final String AUTH_CODE_PREFIX = "AUTH_CODE:";
    public static final String AUTH_SUCCESS_PREFIX = "AUTH_SUCCESS:";
    public static final String MYPAGE_AUTH_CODE_PREFIX = "MYPAGE_AUTH_CODE:";

    // 마이페이지 인증은 검증 1회로 여러 액션(프로필 수정/비밀번호 변경/회원탈퇴)을 커버해야 해서
    // 액션별로 독립된 성공 마커를 둔다. 그래야 한 액션에서 마커를 소비해도 다른 액션이 영향받지 않는다.
    public static final String MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX = "MYPAGE_AUTH_SUCCESS:PROFILE:";
    public static final String MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX = "MYPAGE_AUTH_SUCCESS:PASSWORD:";
    public static final String MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX = "MYPAGE_AUTH_SUCCESS:WITHDRAW:";

    private RedisKeys() {
    }
}
