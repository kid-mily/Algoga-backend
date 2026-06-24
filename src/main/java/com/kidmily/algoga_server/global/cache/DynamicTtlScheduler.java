package com.kidmily.algoga_server.global.cache;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 캐시별 히트율을 주기적으로 보고 TTL 을 자동 조정한다.
 * <ul>
 *   <li>히트율 ≥ 90% → TTL 확장(×1.5): 잘 맞는 캐시는 더 오래 유지해 DB 조회 더 줄임</li>
 *   <li>히트율 &lt; 50% → TTL 축소(×0.5): 잘 안 맞는 캐시는 빨리 비워 신선도 확보 + 메모리 절약</li>
 *   <li>그 사이(50~90%) → 유지</li>
 * </ul>
 * 실제 적용은 {@link DynamicTtlRegistry} 가 baseline 대비 [×0.25, ×4] 로 clamp 한다.
 * 히트율은 <b>직전 측정 대비 증가분(delta)</b>으로 계산해 "최근 구간"의 히트율을 본다.
 * {@code algoga.cache.dynamic-ttl.enabled=false} 로 끌 수 있다(기본 ON).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algoga.cache.dynamic-ttl.enabled", havingValue = "true", matchIfMissing = true)
public class DynamicTtlScheduler {

    private static final double EXPAND_THRESHOLD = 0.90;
    private static final double SHRINK_THRESHOLD = 0.50;
    private static final double EXPAND_FACTOR = 1.5;
    private static final double SHRINK_FACTOR = 0.5;
    /** 이 구간 요청 수가 이보다 적으면 신호 부족 → 조정 스킵 */
    private static final long MIN_SAMPLES = 20;

    private final RedisCacheManager cacheManager;
    private final DynamicTtlRegistry ttlRegistry;
    private final MeterRegistry meterRegistry;

    /** 캐시명 → 직전 스냅샷 [hits, misses] (delta 계산용) */
    private final Map<String, long[]> lastSnapshot = new ConcurrentHashMap<>();

    /** 현재 TTL 을 Prometheus 게이지로 노출 → Grafana 에서 TTL 변동을 시각화 */
    @PostConstruct
    public void registerGauges() {
        for (String cacheName : ttlRegistry.cacheNames()) {
            Gauge.builder("algoga_cache_ttl_seconds", () -> ttlRegistry.getTtl(cacheName).toSeconds())
                    .tag("cache", cacheName)
                    .description("동적으로 조정되는 캐시별 현재 TTL(초)")
                    .register(meterRegistry);
        }
        log.info("[DynamicTTL] TTL 게이지 등록 완료 - 대상 캐시 {}개: {}",
                ttlRegistry.cacheNames().size(), ttlRegistry.cacheNames());
    }

    @Scheduled(
            fixedRateString = "${algoga.cache.dynamic-ttl.interval-ms:3600000}",
            initialDelayString = "${algoga.cache.dynamic-ttl.initial-delay-ms:60000}")
    public void adjustTtls() {
        for (String cacheName : ttlRegistry.cacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (!(cache instanceof RedisCache redisCache)) {
                continue;
            }
            CacheStatistics stats = redisCache.getStatistics();
            long hits = stats.getHits();
            long misses = stats.getMisses();

            long[] prev = lastSnapshot.getOrDefault(cacheName, new long[]{0, 0});
            long deltaHits = Math.max(0, hits - prev[0]);
            long deltaMisses = Math.max(0, misses - prev[1]);
            lastSnapshot.put(cacheName, new long[]{hits, misses});

            long total = deltaHits + deltaMisses;
            if (total < MIN_SAMPLES) {
                log.debug("[DynamicTTL] {} - 표본 부족({}건) → 조정 스킵", cacheName, total);
                continue;
            }

            double hitRate = (double) deltaHits / total;
            Duration current = ttlRegistry.getTtl(cacheName);

            Duration desired;
            String reason;
            if (hitRate >= EXPAND_THRESHOLD) {
                desired = scale(current, EXPAND_FACTOR);
                reason = "확장";
            } else if (hitRate < SHRINK_THRESHOLD) {
                desired = scale(current, SHRINK_FACTOR);
                reason = "축소";
            } else {
                log.info("[DynamicTTL] {} - 히트율 {}% → 유지 (TTL {}s)",
                        cacheName, pct(hitRate), current.toSeconds());
                continue;
            }

            Duration applied = ttlRegistry.updateTtl(cacheName, desired);
            if (applied.toSeconds() == current.toSeconds()) {
                log.info("[DynamicTTL] {} - 히트율 {}% → {} 시도했으나 한계 도달, TTL {}s 유지",
                        cacheName, pct(hitRate), reason, current.toSeconds());
            } else {
                log.info("[DynamicTTL] {} - 히트율 {}% → TTL {} {}s → {}s",
                        cacheName, pct(hitRate), reason, current.toSeconds(), applied.toSeconds());
            }
        }
    }

    private Duration scale(Duration d, double factor) {
        return Duration.ofSeconds(Math.max(1, (long) (d.toSeconds() * factor)));
    }

    private String pct(double rate) {
        return String.format("%.1f", rate * 100);
    }
}
