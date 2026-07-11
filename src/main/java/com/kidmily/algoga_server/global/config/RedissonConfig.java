package com.kidmily.algoga_server.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSslEnabled;

    @Bean
    public RedissonClient redissonClient() {
        // ElastiCache(전송 중 암호화) 접속 시 rediss:// 스킴 사용, 로컬 평문은 redis://
        String scheme = redisSslEnabled ? "rediss://" : "redis://";
        Config config = new Config();
        config.useSingleServer()
                .setAddress(scheme + redisHost + ":" + redisPort)
                // ElastiCache Serverless는 첫 TLS 연결이 지연될 수 있어 여유있게 설정
                .setConnectTimeout(10000)
                .setTimeout(5000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
        return Redisson.create(config);
    }
}
