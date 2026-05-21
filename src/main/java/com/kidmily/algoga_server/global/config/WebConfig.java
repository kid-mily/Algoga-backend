package com.kidmily.algoga_server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 서버의 모든 URL 경로(/**)에 대해 CORS 룰 적용
                .allowedOriginPatterns("*") // 모든 출처(Origin) 허용 (allowCredentials와 혼용 가능)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 모든 HTTP 메서드 허용
                .allowedHeaders("*") // 모든 HTTP 헤더 허용
                .allowCredentials(true) // 쿠키 및 인증 헤더(Authorization 등) 전송 허용
                .exposedHeaders("Authorization", "X-Trace-Id"); // 프론트엔드 브라우저가 접근할 수 있는 응답 헤더 설정
    }
}