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
        Bandwidth burstLimit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(2)));
        Bandwidth sustainedLimit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofHours(1)));
        return BucketConfiguration.builder().addLimit(burstLimit).addLimit(sustainedLimit).build();
    }
}