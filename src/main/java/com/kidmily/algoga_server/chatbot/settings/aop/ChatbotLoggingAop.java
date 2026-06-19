// 파일 경로: src/main/java/com/kidmily/algoga_server/chatbot/settings/aop/ChatbotLoggingAop.java
package com.kidmily.algoga_server.chatbot.settings.aop;

import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ChatbotLoggingAop {

    private final MeterRegistry meterRegistry;

    // 1. 챗봇 전체 비즈니스 로직 (성공/에러/필터링 여부 판별)
    @Pointcut("execution(* com.kidmily.algoga_server.chatbot.application.service.ChatbotCommandService.askToChatbot(..))")
    public void askToChatbotServicePointcut() {}

    // 2. 외부 LLM API (Groq 등) 실제 통신 구간
    @Pointcut("execution(* com.kidmily.algoga_server.chatbot.infrastructure.llm.GroqMainLlmAdapter.generateAnswer(..))")
    public void llmApiPointcut() {}

    // 3. 임베딩 기반 벡터 DB 유사도 필터링 구간
    @Pointcut("execution(* com.kidmily.algoga_server.chatbot.infrastructure.llm.VectorPromptFilterAdapter.isValidQuestion(..))")
    public void vectorFilterPointcut() {}

    @Around("askToChatbotServicePointcut()")
    public Object logAndMetricChatbot(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("[Chatbot Operations] 챗봇 서비스 요청 시작");

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            String status = "success";
            
            // 반환값을 검사하여 도메인 외 질문으로 필터링 되었는지 확인
            if (result instanceof ChatbotAnswerResponse response) {
                if (!response.isSuccess()) {
                    status = "filtered";
                }
            }

            log.info("[Chatbot Operations] 챗봇 서비스 완료 - 상태: {} | 소요시간: {}ms", status, executionTime);

            // 전체 응답 시간 측정
            Timer.builder("algoga_chatbot_execution_time_seconds")
                    .description("Chatbot Overall Execution Time")
                    .tag("status", status)
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);

            // 상태별 호출 횟수 카운트
            meterRegistry.counter("algoga_chatbot_requests_total", "status", status).increment();

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("[Chatbot Operations] 챗봇 서비스 에러 발생 - 소요시간: {}ms | 예외: {}", executionTime, e.getClass().getSimpleName());

            Timer.builder("algoga_chatbot_execution_time_seconds")
                    .tag("status", "error")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);

            meterRegistry.counter("algoga_chatbot_requests_total", "status", "error").increment();
            throw e;
        }
    }

    @Around("llmApiPointcut()")
    public Object logAndMetricLlmApi(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            log.info("[Chatbot Operations] LLM API 정상 응답 수신 - 소요시간: {}ms", executionTime);
            Timer.builder("algoga_chatbot_llm_api_time_seconds")
                    .tag("status", "success")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("[Chatbot Operations] LLM API 장애/지연 - 소요시간: {}ms", executionTime);
            Timer.builder("algoga_chatbot_llm_api_time_seconds")
                    .tag("status", "error")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            throw e;
        }
    }

    @Around("vectorFilterPointcut()")
    public Object logAndMetricVectorFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            
            Timer.builder("algoga_chatbot_filter_time_seconds")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}