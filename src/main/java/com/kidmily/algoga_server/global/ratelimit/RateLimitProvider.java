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

    // 🌟 수정됨: Long userId 대신 String identifier를 받아 범용적으로 사용
    public Bucket getBucket(RateLimitPolicy policy, String identifier) {
        String key = "rate_limit:" + policy.getDomainPrefix() + ":" + identifier;
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        
        // 캐시에 존재하면 가져오고, 없으면 BucketConfiguration에 따라 새로 생성
        return proxyManager.builder().build(keyBytes, policy::getBucketConfiguration);
    }
}