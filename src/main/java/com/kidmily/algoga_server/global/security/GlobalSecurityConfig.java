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

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 누구나 접근 가능해야 하는 곳 (로그인, 회원가입, 스웨거 등)
                        .requestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 2. [403 에러 유도] 관리자 페이지는 ADMIN 권한만 접근 가능
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 3. [401 에러 유도] 유저 관련 API는 반드시 로그인(인증) 필수!
                        .requestMatchers("/api/v1/users/**").authenticated()
                        // 모든 경로에 대해 일단 통과(permitAll)시키도록
                        // (세부 권한은 각 컨트롤러의 @PreAuthorize에서 처리)
                        .anyRequest().permitAll()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint) // 401 (로그인 안 함)
                        .accessDeniedHandler(accessDeniedHandler)           // 403 (권한 없음)
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