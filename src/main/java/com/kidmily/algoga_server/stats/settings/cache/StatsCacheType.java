package com.kidmily.algoga_server.stats.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통계(stats) 도메인 캐시 정의.
 * <p>
 * 관심도(강의별/나라별)는 누적 지표라 기간 파라미터가 없어 캐시 키가 안정적이다.
 * (요약/나라별/강의별을 화면 렌더마다 각각 재조회하던 것을 캐시로 흡수한다)
 * 누적 카운트라 최대 TTL 만큼의 지연은 허용된다(어드민 분석 화면 성격).
 */
@Getter
@RequiredArgsConstructor
public enum StatsCacheType {

    // 관심도 요약 카드 (TTL: 10분)
    INTEREST_SUMMARY(Const.INTEREST_SUMMARY, 10 * 60),
    // 나라별 관심도 (TTL: 10분)
    INTEREST_COUNTRIES(Const.INTEREST_COUNTRIES, 10 * 60),
    // 강의별 관심도 (TTL: 10분)
    INTEREST_LECTURES(Const.INTEREST_LECTURES, 10 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    // 어노테이션(@Cacheable)에서 사용할 수 있도록 정적 상수 정의
    public static class Const {
        public static final String INTEREST_SUMMARY = "statsInterestSummary";
        public static final String INTEREST_COUNTRIES = "statsInterestCountries";
        public static final String INTEREST_LECTURES = "statsInterestLectures";
    }
}
