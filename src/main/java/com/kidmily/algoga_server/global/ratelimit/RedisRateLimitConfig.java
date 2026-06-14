// global/ratelimit/RedisRateLimitConfig.java
package com.kidmily.algoga_server.global.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisRateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Bean
    public ProxyManager<byte[]> lettuceProxyManager() {
        try {
            RedisURI.Builder uriBuilder = RedisURI.builder()
                    .withHost(redisHost)
                    .withPort(redisPort);

            if (redisPassword != null && !redisPassword.isBlank()) {
                uriBuilder.withPassword(redisPassword.toCharArray());
            }

            RedisClient redisClient = RedisClient.create(uriBuilder.build());
            StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(ByteArrayCodec.INSTANCE);

            // 🌟 수정됨: 도메인별 최대 제한 시간(1시간)보다 넉넉하게 2시간으로 TTL 연장
            ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                    .withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofHours(2)));

            log.info("[Bucket4j] Redis Proxy Manager가 성공적으로 연결되었습니다. (Host: {})", redisHost);
            return LettuceBasedProxyManager.builderFor(connection)
                    .withClientSideConfig(clientSideConfig)
                    .build();

        } catch (Exception e) {
            log.error("[Bucket4j] Redis 연결 또는 ProxyManager 생성 실패! 원인: {}", e.getMessage());
            throw new IllegalStateException("Redis 연동 실패. Redis 서버가 켜져있는지 확인하세요.", e);
        }
    }
}