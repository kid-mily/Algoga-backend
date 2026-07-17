package com.kidmily.algoga_server.chatbot.infrastructure.config;

import com.kidmily.algoga_server.global.circuitbreaker.CircuitBreakerPolicy;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import java.time.Duration;

@Component
public class ChatbotCircuitBreakerConfig implements CircuitBreakerPolicy {

    @Override
    public String getCircuitBreakerName() {
        return "pythonRagApi";
    }

    @Override
    public CircuitBreakerConfig getCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(10))
                .slowCallRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .permittedNumberOfCallsInHalfOpenState(2)
                .ignoreExceptions(HttpClientErrorException.class)
                .build();
    }
}