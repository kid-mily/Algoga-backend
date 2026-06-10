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
            // 🌟 여기서 Redis 연결을 시도합니다. (꺼져있으면 예외 발생)
            StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(ByteArrayCodec.INSTANCE);

            ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                    .withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(10)));

            log.info("[Bucket4j] Redis Proxy Manager가 성공적으로 연결되었습니다. (Host: {})", redisHost);
            return LettuceBasedProxyManager.builderFor(connection)
                    .withClientSideConfig(clientSideConfig)
                    .build();

        } catch (Exception e) {
            log.error("[Bucket4j] Redis 연결 또는 ProxyManager 생성 실패! 원인: {}", e.getMessage());
            // Redis가 꺼져있어 연결 실패 시, 스프링이 터지지 않도록 null 반환 (실제 API 호출 시점에 에러가 남)
            throw new IllegalStateException("Redis 연동 실패. Redis 서버가 켜져있는지 확인하세요.", e);
        }
    }
}