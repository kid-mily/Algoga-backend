package com.kidmily.algoga_server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 🚨 주의: GlobalSecurityConfig의 CORS와 충돌하여
        // "다중 Access-Control-Allow-Origin 헤더" 에러를 유발하므로 비워둡니다!
        // CORS 설정은 시큐리티 설정에서 전담합니다.
    }
}