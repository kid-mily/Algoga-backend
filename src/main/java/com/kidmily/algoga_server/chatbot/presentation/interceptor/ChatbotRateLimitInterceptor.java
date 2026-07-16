// chatbot/presentation/interceptor/ChatbotRateLimitInterceptor.java
package com.kidmily.algoga_server.chatbot.presentation.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.config.ChatbotRateLimitConfig;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.ratelimit.RateLimitProvider;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ChatbotRateLimitInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final RateLimitProvider rateLimitProvider;
    private final ChatbotRateLimitConfig chatbotRateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("true".equals(System.getProperty("LOAD_TEST_MODE"))) {
            return true;
        }
        // 🌟 1. 해시 충돌 없는 안전한 String 식별자 추출
        String identifier = extractUserIdentifier(request);

        // 🌟 2. 해당 식별자의 전용 버킷 가져오기
        Bucket bucket = rateLimitProvider.getBucket(chatbotRateLimitConfig, identifier);

        // 🌟 3. 버킷에서 토큰 1개 소비 시도
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            // 🚫 제한 초과 시 차단
            long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            sendRateLimitResponse(response, waitForRefillSeconds);
            return false;
        }
        
        return true;
    }

    /**
     * SecurityContext에서 로그인한 유저의 ID를 추출합니다.
     * 비로그인 환경일 경우 IP 주소를 그대로 사용하여 충돌을 방지합니다.
     */
    private String extractUserIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return "USER_" + userDetails.getUser().getId();
        }
        
        // 비로그인 상태이거나 토큰이 없는 경우 (IP 주소 원본 사용)
        return "IP_" + request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, long waitForRefillSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(waitForRefillSeconds)); 
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String gentleText = "짧은 시간에 너무 많은 질문이 입력되었습니다. 안정적인 답변 생성을 위해 잠시 후 다시 질문해 주시면 친절히 안내해 드릴게요! 😊";
        ChatbotAnswerResponse answerResponse = ChatbotAnswerResponse.rateLimited(gentleText);
        ApiResponse<ChatbotAnswerResponse> apiResponse = ApiResponse.success("CHATBOT_RATE_LIMITED", "요청 제한 적용", answerResponse);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}