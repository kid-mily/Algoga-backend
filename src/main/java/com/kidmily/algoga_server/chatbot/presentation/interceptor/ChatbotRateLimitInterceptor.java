// chatbot/presentation/interceptor/ChatbotRateLimitInterceptor.java
package com.kidmily.algoga_server.chatbot.presentation.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.config.ChatbotRateLimitConfig;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.ratelimit.RateLimitProvider;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatbotRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitProvider rateLimitProvider;
    private final ChatbotRateLimitConfig chatbotPolicy;
    private final ObjectMapper objectMapper; // 🌟 유한 대답 JSON 변환용 추가

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long userId = userDetails.getUser().getId();
            Bucket bucket = rateLimitProvider.getBucket(chatbotPolicy, userId);

            // 🌟 토큰 차감 시도 (1분당 30회 혹은 2초당 1회 등 설정에 따름)
            if (!bucket.tryConsume(1)) {
                log.warn("[Rate Limit] 유저 ID: {} 가 요청 제한을 초과하여 최전방 차단되었습니다.", userId);
                
                // 에러를 던지지 않고, 챗봇 대답 형태로 부드럽게 응답 처리
                sendGentleResponse(response); 
                return false; // 🛑 컨트롤러/벡터필터로 요청을 넘기지 않고 여기서 즉시 통신 종료 (비용 세이브!)
            }
        }
        return true;
    }

    /**
     * 🌟 빨간 에러 창 대신 챗봇 말풍선에 들어갈 부드러운 응답을 강제 주입하는 메서드
     */
    private void sendGentleResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK로 유하게 전달
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 사용자가 상처받지 않도록 유하게 작성한 텍스트
        String gentleText = "짧은 시간에 너무 많은 질문이 입력되었습니다. 안정적인 답변 생성을 위해 잠시 후 다시 질문해 주시면 친절히 안내해 드릴게요! 😊";
        
        ChatbotAnswerResponse answerResponse = new ChatbotAnswerResponse(gentleText, false);
        ApiResponse<ChatbotAnswerResponse> apiResponse = ApiResponse.success("CHATBOT_RATE_LIMITED", "요청 제한 적용", answerResponse);

        // 오브젝트 매퍼로 JSON 직렬화 후 응답 스트림에 쏘기
        String jsonResult = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResult);
    }
}