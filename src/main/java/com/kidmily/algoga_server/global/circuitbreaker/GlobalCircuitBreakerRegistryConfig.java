package com.kidmily.algoga_server.global.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class GlobalCircuitBreakerRegistryConfig {

    private final CircuitBreakerRegistry registry;
    // 모든 도메인의 서킷 브레이커 정책을 자동으로 주입받음
    private final List<CircuitBreakerPolicy> policies;

    @PostConstruct
    public void registerDomainPolicies() {
        for (CircuitBreakerPolicy policy : policies) {
            // 도메인이 정의한 이름과 설정값으로 레지스트리에 등록
            registry.circuitBreaker(policy.getCircuitBreakerName(), policy.getCircuitBreakerConfig());
        }
    }
}