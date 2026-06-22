package com.kidmily.algoga_server.booking.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingCacheType {

    // 내 예약 목록 캐시 (TTL: 5분)
    MY_BOOKINGS(Const.MY_BOOKINGS, 5 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    // 어노테이션(@Cacheable/@CacheEvict)에서 사용할 수 있도록 정적 상수 정의
    public static class Const {
        public static final String MY_BOOKINGS = "myBookings";
    }
}
