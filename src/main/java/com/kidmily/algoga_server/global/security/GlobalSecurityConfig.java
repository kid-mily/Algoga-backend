package com.kidmily.algoga_server.global.config;

import com.kidmily.algoga_server.global.security.GlobalJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 컨트롤러의 @PreAuthorize 처리를 위해 필수
@RequiredArgsConstructor
public class GlobalSecurityConfig {

    private final GlobalJwtAuthenticationFilter globalJwtAuthenticationFilter;

    // 공통 PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🌟 요청하신 대로 모든 경로에 대해 일단 통과(permitAll)시키도록 변경
                        // (세부 권한은 각 컨트롤러의 @PreAuthorize에서 처리)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(globalJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}