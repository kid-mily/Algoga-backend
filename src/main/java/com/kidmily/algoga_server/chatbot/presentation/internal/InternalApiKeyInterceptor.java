package com.kidmily.algoga_server.chatbot.presentation.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * /internal/** 경로를 공유 시크릿(X-Internal-Api-Key)으로 보호하는 인터셉터.
 * Python RAG 서버만 이 헤더를 알고 있으므로, 외부에서 내부 API 를 찌르는 것을 막는다.
 *
 * fail-closed: 시크릿이 설정되지 않았으면 전부 거부한다(실수로 내부 API 가 열리는 것 방지).
 */
@Slf4j
@Component
public class InternalApiKeyInterceptor implements HandlerInterceptor {

    private static final String HEADER = "X-Internal-Api-Key";

    private final String apiKey;

    public InternalApiKeyInterceptor(@Value("${chatbot.internal.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("[내부 API] chatbot.internal.api-key 가 설정되지 않아 내부 호출을 거부합니다.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }

        String provided = request.getHeader(HEADER);
        if (!apiKey.equals(provided)) {
            log.warn("[내부 API] 유효하지 않은 {} 헤더로 접근 차단: {}", HEADER, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
