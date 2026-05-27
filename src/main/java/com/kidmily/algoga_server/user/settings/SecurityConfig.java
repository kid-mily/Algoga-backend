package com.kidmily.algoga_server.user.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 유저 전용 필터 주입

    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. REST API 환경을 위해 CSRF 방어 비활성화 (스웨거에서 POST 요청 쏠 때 403 방지)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. JWT 인증이므로 세션을 사용하지 않도록 무상태(Stateless) 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 🌟 [추가] 스웨거 UI 및 API 문서 관련 경로 전면 개방
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 3. /public/ 으로 시작하는 모든 API는 통과
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // 4. 로그인/회원가입 관련 (매니저 로그인 /api/v1/auth/admin/login 도 여기 포함되어 통과됨)
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 5. 그 외의 나머지 모든 유저 API는 로그인(토큰) 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}