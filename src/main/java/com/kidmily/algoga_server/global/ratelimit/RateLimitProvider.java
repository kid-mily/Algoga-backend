// global/ratelimit/RateLimitProvider.java
package com.kidmily.algoga_server.global.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RateLimitProvider {

    private final ProxyManager<byte[]> proxyManager;

    public Bucket getBucket(RateLimitPolicy policy, Long userId) {
        String key = "rate_limit:" + policy.getDomainPrefix() + ":" + userId;
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // 도메인이 만든 섬세한 BucketConfiguration을 그대로 사용!
        return proxyManager.builder().build(keyBytes, policy::getBucketConfiguration);
    }
}