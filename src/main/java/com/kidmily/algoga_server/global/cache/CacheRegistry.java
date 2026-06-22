package com.kidmily.algoga_server.global.cache;

import java.time.Duration;
import java.util.Map;

/**
 * 각 도메인이 자기 캐시 설정(캐시 이름 → TTL)을 등록하기 위한 공통 인터페이스.
 * <p>
 * 도메인별로 이 인터페이스의 구현체(@Component)를 두면, {@link CacheManagerConfig} 가
 * 모든 구현체를 모아 RedisCacheManager 에 캐시별 TTL 을 등록한다.
 * 새 캐시를 추가할 땐 RedisConfig 를 건드리지 않고 해당 도메인의 구현체만 수정하면 된다.
 */
public interface CacheRegistry {

    Map<String, Duration> getCacheConfigurations();
}
