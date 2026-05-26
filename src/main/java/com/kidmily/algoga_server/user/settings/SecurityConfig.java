package com.kidmily.algoga_server.user.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 🌟 유저 전용 필터 주입

    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        // UserSecurityConfig.java
        http
                .authorizeHttpRequests(auth -> auth
                        // 🌟 /public/ 으로 시작하는 모든 API는 통과! (앞으로 도메인이 백 개 생겨도 수정 불필요)
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // 로그인 관련
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 나머지는 모두 로그인(토큰) 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}