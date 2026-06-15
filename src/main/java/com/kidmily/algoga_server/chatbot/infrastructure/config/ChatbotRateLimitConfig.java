package com.kidmily.algoga_server.chatbot.infrastructure.config;

import com.kidmily.algoga_server.global.ratelimit.RateLimitPolicy;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ChatbotRateLimitConfig implements RateLimitPolicy {

    @Override
    public String getDomainPrefix() {
        return "chatbot";
    }

    @Override
    public BucketConfiguration getBucketConfiguration() {
        
        // 🚨 1차 방어 (Burst Limit - 광클릭/따닥 방지)
        // 조건: 2초당 최대 1번만 요청 가능 (순간적인 트래픽 폭주 차단)
        Bandwidth burstLimit = Bandwidth.classic(
                1, 
                Refill.intervally(1, Duration.ofSeconds(2))
        );

        // 🛡️ 2차 방어 (Sustained Limit - 매크로/과다 사용 방지)
        // 조건: 1시간당 최대 30번만 요청 가능 (LLM API 비용 방어)
        Bandwidth sustainedLimit = Bandwidth.classic(
                30, 
                Refill.intervally(30, Duration.ofHours(1))
        );

        // 🌟 다중 대역폭(Bandwidth)을 하나의 버킷에 겹쳐서 적용!
        // 두 조건 중 하나라도 위반하면 즉시 요청이 차단(429 Error)됩니다.
        return BucketConfiguration.builder()
                .addLimit(burstLimit)
                .addLimit(sustainedLimit)
                .build();
    }
}