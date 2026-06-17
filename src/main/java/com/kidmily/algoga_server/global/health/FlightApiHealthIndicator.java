package com.kidmily.algoga_server.global.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component("flightApi")
@RequiredArgsConstructor
public class FlightApiHealthIndicator implements HealthIndicator {

    private static final String CB_NAME = "flight";

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        CircuitBreaker.State state = cb.getState();
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        return switch (state) {
            case CLOSED -> Health.up()
                    .withDetail("circuitBreaker", "CLOSED")
                    .withDetail("failureRate", metrics.getFailureRate() + "%")
                    .withDetail("bufferedCalls", metrics.getNumberOfBufferedCalls())
                    .withDetail("failedCalls", metrics.getNumberOfFailedCalls())
                    .build();

            case OPEN -> Health.down()
                    .withDetail("circuitBreaker", "OPEN")
                    .withDetail("failureRate", metrics.getFailureRate() + "%")
                    .withDetail("failedCalls", metrics.getNumberOfFailedCalls())
                    .withDetail("reason", "항공편 API 장애로 서킷브레이커가 열렸습니다.")
                    .build();

            case HALF_OPEN -> Health.unknown()
                    .withDetail("circuitBreaker", "HALF_OPEN")
                    .withDetail("failureRate", metrics.getFailureRate() + "%")
                    .withDetail("reason", "항공편 API 복구 여부 확인 중입니다.")
                    .build();

            default -> Health.unknown()
                    .withDetail("circuitBreaker", state.name())
                    .build();
        };
    }
}
