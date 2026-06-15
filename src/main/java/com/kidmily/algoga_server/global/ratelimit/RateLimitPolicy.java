// global/ratelimit/RateLimitPolicy.java
package com.kidmily.algoga_server.global.ratelimit;

import io.github.bucket4j.BucketConfiguration;

public interface RateLimitPolicy {
    // 1. 도메인 식별자 (Redis Key)
    String getDomainPrefix();
    
    // 2. 도메인이 직접 세팅한 다중 Bucket 설정 객체를 반환
    BucketConfiguration getBucketConfiguration();
}