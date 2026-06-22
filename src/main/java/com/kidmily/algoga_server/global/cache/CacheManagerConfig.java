package com.kidmily.algoga_server.global.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 모든 도메인의 {@link CacheRegistry} 구현체를 모아 RedisCacheManager 를 구성한다.
 * <p>
 * 각 도메인은 자기 캐시(이름 → TTL)만 {@link CacheRegistry} 로 등록하고,
 * 글로벌은 그것들을 합쳐 등록만 한다(도메인 지식을 글로벌이 떠안지 않음).
 * TTL 미지정 캐시는 기본 10분이 적용된다.
 */
@Configuration
@EnableCaching
public class CacheManagerConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                          List<CacheRegistry> cacheRegistries) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // 각 도메인 CacheRegistry 가 등록한 캐시별 TTL 을 모아서 적용
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        for (CacheRegistry registry : cacheRegistries) {
            registry.getCacheConfigurations().forEach((cacheName, ttl) ->
                    cacheConfigurations.put(cacheName, defaultConfig.entryTtl(ttl)));
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
