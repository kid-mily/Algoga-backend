package com.kidmily.algoga_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 캐시(@Cacheable/@CacheEvict)용 CacheManager 와 도메인별 TTL 설정은
    // global/cache 의 CacheManagerConfig + 각 도메인 CacheRegistry 로 이전됨.
    // 여기는 일반 RedisTemplate 만 제공한다 (이메일 인증코드 등 수동 Redis 사용처용).

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
