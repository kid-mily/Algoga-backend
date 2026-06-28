package com.kidmily.algoga_server.chatbot.settings.aop;

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

    // 변경된 메서드명에 맞게 Pointcut 수정
    @Pointcut("execution(* com.kidmily.algoga_server.chatbot.application.service.ChatbotLoadTestService.searchFromMysqlWithVectorCalc(..))")
    public void mysqlVectorCalcPointcut() {}

    @Pointcut("execution(* com.kidmily.algoga_server.chatbot.application.service.ChatbotLoadTestService.searchFromRedisVector(..))")
    public void redisVectorPointcut() {}

    // 🌟 태그를 "mysql" 로 고정!
    @Around("mysqlVectorCalcPointcut()")
    public Object measureMysqlTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithTimer(joinPoint, "mysql"); 
    }

    // 🌟 태그를 "vector" 로 고정!
    @Around("redisVectorPointcut()")
    public Object measureRedisTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithTimer(joinPoint, "vector"); 
    }

    private Object executeWithTimer(ProceedingJoinPoint joinPoint, String scenario) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            recordMetrics(scenario, start, "success");
            return result;
        } catch (Exception e) {
            recordMetrics(scenario, start, "error");
            throw e;
        }
    }

    private void recordMetrics(String scenario, long startTime, String status) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        Timer.builder("algoga_chatbot_search_time_seconds")
                .tag("scenario", scenario)
                .tag("status", status)
                .publishPercentileHistogram() // 그라파나 p95 렌더링 필수!
                .register(meterRegistry)
                .record(executionTime, TimeUnit.MILLISECONDS);
    }
}