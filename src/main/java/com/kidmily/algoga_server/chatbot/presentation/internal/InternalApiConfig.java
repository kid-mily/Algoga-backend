package com.kidmily.algoga_server.chatbot.presentation.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * /internal/** 경로에 API 키 인터셉터를 등록한다.
 */
@Configuration
@RequiredArgsConstructor
public class InternalApiConfig implements WebMvcConfigurer {

    private final InternalApiKeyInterceptor internalApiKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiKeyInterceptor)
                .addPathPatterns("/internal/**");
    }
}
