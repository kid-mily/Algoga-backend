package com.kidmily.algoga_server.global.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

public interface CircuitBreakerPolicy {
    // 서킷 브레이커의 고유 식별자 (예: "groqLlmApi", "tossPaymentApi")
    String getCircuitBreakerName();
    
    // 도메인 맞춤형 차단 룰 (실패율, 대기 시간 등)
    CircuitBreakerConfig getCircuitBreakerConfig();
}