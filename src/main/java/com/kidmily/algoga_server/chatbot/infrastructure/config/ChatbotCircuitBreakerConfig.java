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
        return "groqLlmApi";
    }

    @Override
    public CircuitBreakerConfig getCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // [기본 실패율 룰]
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)          // 최근 10번 호출을 평가
                .minimumNumberOfCalls(5)        // 최소 5번은 호출되어야 평가 시작
                .failureRateThreshold(50)       // 에러율이 50%를 넘으면 서킷 오픈(차단)

                // ⏱️ [고급: 느린 응답(Slow Call) 차단 룰]
                // LLM 서버가 죽진 않았지만 너무 느려서 우리 서버 스레드가 고갈되는 것을 방지
                .slowCallDurationThreshold(Duration.ofSeconds(10)) // 10초 이상 걸리면 느린 응답으로 간주
                .slowCallRateThreshold(40)                         // 느린 응답 비율이 40% 이상이면 차단

                // ♻️ [고급: 차단 후 자동 회복 룰]
                .waitDurationInOpenState(Duration.ofSeconds(20)) // 20초 차단 후 
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // 스스로 반열림(Half-Open) 상태로 전환하여 테스트
                .permittedNumberOfCallsInHalfOpenState(2)        // 반열림 상태에서 2번 찔러보고 정상이면 닫음(Close)

                // 🛑 [고급: 특정 예외 무시]
                // 4xx 에러(우리가 프롬프트를 잘못 짠 경우 등)는 외부 서버 장애가 아니므로 실패율로 카운트하지 않음!
                .ignoreExceptions(HttpClientErrorException.class) 
                
                .build();
    }
}