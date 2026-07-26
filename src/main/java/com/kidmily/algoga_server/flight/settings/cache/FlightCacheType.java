package com.kidmily.algoga_server.flight.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 항공편(flight) 도메인 캐시 정의.
 * <p>
 * 항공편 실시간 조회는 (목적지, 출발일)이 같으면 결과가 같은 외부 API 호출이다.
 * 패키지 목록은 조회할 때마다 패키지 수만큼 이 조회를 하므로, 짧은 TTL 캐시로 반복 호출을 흡수한다.
 * 스케줄이 자주 바뀌지 않고 데모 성격이라 분 단위 지연은 허용된다(TTL 로 바운드).
 */
@Getter
@RequiredArgsConstructor
public enum FlightCacheType {

    // 항공편 실시간 조회 (TTL: 10분)
    FLIGHT_SEARCH(Const.FLIGHT_SEARCH, 10 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    // 어노테이션(@Cacheable)에서 사용할 수 있도록 정적 상수 정의
    public static class Const {
        public static final String FLIGHT_SEARCH = "flightSearch";
    }
}
