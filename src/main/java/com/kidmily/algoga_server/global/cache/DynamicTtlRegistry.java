package com.kidmily.algoga_server.global.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 캐시별 "현재 TTL"을 런타임에 바꿀 수 있게 보관하는 저장소.
 * <p>
 * Spring 의 {@code RedisCacheManager} 는 TTL 을 빌드 시점에 고정하므로, 런타임 조정을 위해
 * {@link CacheManagerConfig} 가 각 캐시에 {@code TtlFunction} 을 걸어 <b>매 캐시 쓰기마다</b>
 * 이 저장소의 현재 TTL 을 읽게 한다. {@code DynamicTtlScheduler} 는 히트율을 보고 여기의 값만 갱신한다.
 * <p>
 * baseline(초기 TTL) 대비 일정 배수 범위로 clamp 해서 과도한 확장/축소를 막는다.
 */
@Component
public class DynamicTtlRegistry {

    /** baseline 대비 최소/최대 배수 (예: 1시간 baseline → 15분 ~ 4시간 사이로만 움직임) */
    private static final double MIN_FACTOR = 0.25;
    private static final double MAX_FACTOR = 4.0;
    private static final Duration ABSOLUTE_FLOOR = Duration.ofSeconds(10);

    private final Map<String, Duration> baselineTtls = new ConcurrentHashMap<>();
    private final Map<String, Duration> currentTtls = new ConcurrentHashMap<>();

    /** 캐시 등록 (CacheManagerConfig 가 빌드 시 호출). 현재 TTL 은 baseline 으로 초기화. */
    public void register(String cacheName, Duration baseline) {
        baselineTtls.put(cacheName, baseline);
        currentTtls.putIfAbsent(cacheName, baseline);
    }

    /** TtlFunction 이 매 쓰기마다 호출 → 현재 TTL 반환. */
    public Duration getTtl(String cacheName) {
        return currentTtls.getOrDefault(cacheName,
                baselineTtls.getOrDefault(cacheName, Duration.ofMinutes(10)));
    }

    public Duration getBaseline(String cacheName) {
        return baselineTtls.get(cacheName);
    }

    /**
     * 원하는 TTL 로 갱신하되 baseline 대비 [MIN_FACTOR, MAX_FACTOR] 로 clamp.
     * @return 실제 적용된 TTL (clamp 결과)
     */
    public Duration updateTtl(String cacheName, Duration desired) {
        Duration baseline = baselineTtls.getOrDefault(cacheName, desired);
        long minSec = Math.max(ABSOLUTE_FLOOR.toSeconds(), (long) (baseline.toSeconds() * MIN_FACTOR));
        long maxSec = (long) (baseline.toSeconds() * MAX_FACTOR);
        long clampedSec = Math.max(minSec, Math.min(maxSec, desired.toSeconds()));
        Duration clamped = Duration.ofSeconds(clampedSec);
        currentTtls.put(cacheName, clamped);
        return clamped;
    }

    /** 한계(min/max)에 도달했는지 — 로그용 */
    public boolean isAtBound(String cacheName) {
        Duration baseline = baselineTtls.get(cacheName);
        if (baseline == null) return false;
        long cur = getTtl(cacheName).toSeconds();
        long minSec = Math.max(ABSOLUTE_FLOOR.toSeconds(), (long) (baseline.toSeconds() * MIN_FACTOR));
        long maxSec = (long) (baseline.toSeconds() * MAX_FACTOR);
        return cur <= minSec || cur >= maxSec;
    }

    public Set<String> cacheNames() {
        return baselineTtls.keySet();
    }
}
