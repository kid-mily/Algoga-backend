package com.kidmily.algoga_server.payment.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentCacheType {

    // 내 결제 목록 캐시 (TTL: 5분)
    MY_PAYMENTS(Const.MY_PAYMENTS, 5 * 60),
    // 어드민 결제 통계 캐시 (TTL: 1시간)
    ADMIN_PAYMENT_STATS(Const.ADMIN_PAYMENT_STATS, 60 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    // 어노테이션(@Cacheable/@CacheEvict)에서 사용할 수 있도록 정적 상수 정의
    public static class Const {
        public static final String MY_PAYMENTS = "myPayments";
        public static final String ADMIN_PAYMENT_STATS = "adminPaymentStats";
    }
}
