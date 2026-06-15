// chatbot/presentation/config/ChatbotWebConfig.java
package com.kidmily.algoga_server.chatbot.presentation.config;

import com.kidmily.algoga_server.chatbot.presentation.interceptor.ChatbotRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ChatbotWebConfig implements WebMvcConfigurer {

    private final ChatbotRateLimitInterceptor chatbotRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 🌟 /api/v1/chatbot/ask 경로로 들어오는 요청만 인터셉터가 가로채서 검사하도록 설정
        registry.addInterceptor(chatbotRateLimitInterceptor)
                .addPathPatterns("/api/v1/chatbot/ask");
    }
}