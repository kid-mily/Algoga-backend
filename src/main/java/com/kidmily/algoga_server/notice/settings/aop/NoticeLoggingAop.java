package com.kidmily.algoga_server.notice.settings.aop;

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
public class NoticeLoggingAop {

    private final MeterRegistry meterRegistry;

    @Pointcut("execution(* com.kidmily.algoga_server.notice.presentation.api..*Controller.*(..))")
    public void noticeControllerPointcut() {}

    @Pointcut("execution(* com.kidmily.algoga_server.notice.application.service..*Service.*(..))")
    public void noticeServicePointcut() {}

    @Around("noticeControllerPointcut() || noticeServicePointcut()")
    public Object logAndMetricNotice(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 🌟 1ms 미만의 정밀 측정을 위해 System.nanoTime() 사용
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long executionTimeNano = System.nanoTime() - start;

            Timer.builder("algoga_notice_execution_time_seconds")
                    .description("Notice Domain Method Execution Time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .record(executionTimeNano, TimeUnit.NANOSECONDS); // 🌟 나노초 단위로 기록

            return result;
        } catch (Exception e) {
            long executionTimeNano = System.nanoTime() - start;

            Timer.builder("algoga_notice_execution_time_seconds")
                    .description("Notice Domain Method Execution Time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .register(meterRegistry)
                    .record(executionTimeNano, TimeUnit.NANOSECONDS); // 🌟 나노초 단위로 기록
                    
            meterRegistry.counter("algoga_notice_errors_total", "class", className, "method", methodName).increment();
            throw e;
        }
    }
}