package com.kidmily.algoga_server.banner.settings.aop;

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
public class BannerLoggingAop {

    // 프로메테우스 연동을 위한 메트릭 레지스트리
    private final MeterRegistry meterRegistry;

    // 배너 Controller 계층 포인트컷
    @Pointcut("execution(* com.kidmily.algoga_server.banner.presentation.api..*Controller.*(..))")
    public void bannerControllerPointcut() {}

    // 배너 Service 계층 포인트컷
    @Pointcut("execution(* com.kidmily.algoga_server.banner.application.service..*Service.*(..))")
    public void bannerServicePointcut() {}

    @Around("bannerControllerPointcut() || bannerServicePointcut()")
    public Object logAndMetricBanner(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 1. [Loki 연동] 시작 로그 기록
        log.info("[Banner Domain] Request Start - Class: {}, Method: {}", className, methodName);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            // 2. [Loki 연동] 성공 로그 기록
            log.info("[Banner Domain] Request Success - Class: {}, Method: {} | Time: {}ms", className, methodName, executionTime);

            // 3. [Prometheus 연동] 실행 시간 및 호출 횟수 메트릭 기록
            Timer.builder("algoga_banner_execution_time_seconds")
                    .description("Banner Domain Method Execution Time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            
            // 4. [Loki 연동] 에러 로그 기록
            log.error("[Banner Domain] Request Error - Class: {}, Method: {} | Time: {}ms | Exception: {}", 
                    className, methodName, executionTime, e.getClass().getSimpleName());

            // 5. [Prometheus 연동] 에러 발생 횟수 및 시간 메트릭 기록
            Timer.builder("algoga_banner_execution_time_seconds")
                    .description("Banner Domain Method Execution Time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
                    
            meterRegistry.counter("algoga_banner_errors_total", "class", className, "method", methodName).increment();

            throw e;
        }
    }
}