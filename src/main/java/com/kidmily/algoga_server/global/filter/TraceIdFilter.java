package com.kidmily.algoga_server.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    // GlobalExceptionHandler에서도 공유할 수 있도록 public으로 변경
    public static final String TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Trace ID 생성 (UUID 앞 8자리)
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);

        // 비동기 스레드나 인터셉터 등에서 꺼내 쓸 수 있도록 request 컨텍스트에도 저장
        request.setAttribute(TRACE_ID_KEY, traceId);

        // 2. 프론트엔드 응답 헤더에 추가
        response.setHeader("X-Trace-Id", traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 3. 스레드 풀 누수 방지를 위한 자원 해제
            MDC.clear();
        }
    }
}