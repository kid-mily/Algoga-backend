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
    public static final String MYPAGE_AUTH_SUCCESS_PREFIX = "MYPAGE_AUTH_SUCCESS:";

    private RedisKeys() {
    }
}
