package com.kidmily.algoga_server.global.security;

import com.kidmily.algoga_server.global.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 컨트롤러의 @PreAuthorize 처리를 위해 필수
@RequiredArgsConstructor
public class GlobalSecurityConfig {

    private final GlobalJwtAuthenticationFilter globalJwtAuthenticationFilter;

    // 구글에서 받아온 유저 정보를 처리할 서비스
    private final CustomOAuth2UserService customOAuth2UserService;

    // 소셜 로그인 성공 시 JWT 토큰을 발급하고 프론트엔드로 보내줄 핸들러
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

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
                // OAuth2 소셜 로그인 설정 시작
                .oauth2Login(oauth2 -> oauth2
                        // 구글 로그인 성공 후, 구글 서버에서 사용자 정보(이메일, 이름 등)를 가져온 상태에서 실행됨
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 유저 정보 처리까지 다 성공하면 이 핸들러를 실행해라!
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(globalJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}