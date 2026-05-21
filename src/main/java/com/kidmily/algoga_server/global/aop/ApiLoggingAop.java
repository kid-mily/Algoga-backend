package com.kidmily.algoga_server.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class ApiLoggingAop {

    // 클린 아키텍처 구조에 맞게 presentation 계층의 모든 Controller를 타겟팅합니다.
    // (만약 Controller 패키지 구조가 다르면 아래 경로를 수정하세요)
    @Pointcut("execution(* com.kidmily.algoga_server.*..presentation..*Controller.*(..))")
    private void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String method = request.getMethod();
        String requestUri = request.getRequestURI();
        String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 1. 요청 로그
        log.info("[API Request] {} {} | Controller: {}.{}()", method, requestUri, controllerName, methodName);

        long startTime = System.currentTimeMillis();

        try {
            // 2. 실제 컨트롤러 로직 실행
            Object result = joinPoint.proceed();

            // 3. 성공 응답 로그 및 실행 시간 측정
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[API Response] {} {} | Time: {}ms", method, requestUri, executionTime);

            return result;
        } catch (Exception e) {
            // 4. 예외 발생 시 로그 (GlobalExceptionHandler로 넘어가기 전)
            long executionTime = System.currentTimeMillis() - startTime;
            log.warn("[API Error] {} {} | Time: {}ms | Exception: {}", method, requestUri, executionTime, e.getClass().getSimpleName());
            throw e; // 예외를 던져서 GlobalExceptionHandler가 처리하게 함
        }
    }
}