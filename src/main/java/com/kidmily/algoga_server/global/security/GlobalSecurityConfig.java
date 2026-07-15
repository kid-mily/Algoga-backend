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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class GlobalSecurityConfig {

    private final GlobalJwtAuthenticationFilter globalJwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 🌟 1. 시큐리티 체인에 CORS 설정 추가 (핵심!)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 누구나 접근 가능해야 하는 곳 (로그인, 회원가입, 스웨거 등)
                        .requestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/**", "/swagger-ui/**", "/v3/api-docs/**", "/ws/chat", "/ws/chat/**").permitAll()

                        // 2. [403 에러 유도] 관리자 페이지는 ADMIN들만 접근 가능
                        .requestMatchers("/api/v1/admin/**", "/api/v1/*/admin/**")
                        .hasAnyRole("SUPER_ADMIN", "CS_MANAGER", "CONTENT_MANAGER", "SETTLEMENT_MANAGER", "STATISTICS_MANAGER")



                        // 3. [401 에러 유도] 유저 관련 API는 반드시 로그인(인증) 필수!
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .requestMatchers("/api/v1/chat/**").authenticated()
                        .requestMatchers("/api/v1/friends/**").authenticated()
                        // 예약/결제/환불은 로그인 유저 전제(컨트롤러가 인증 principal 사용) → 미인증은 500(NPE)이 아니라 401로 떨어지게
                        .requestMatchers("/api/v1/bookings/**").authenticated()
                        // 결제: 웹훅만 외부(PortOne)가 호출하므로 공개, 나머지 결제 API는 로그인 필수 (permitAll보다 먼저 매칭돼야 함)
                        .requestMatchers("/api/v1/payments/webhook").permitAll()
                        .requestMatchers("/api/v1/payments/**").authenticated()
                        // 환불 요청(유저): 로그인 필수 (어드민 환불은 위 /api/v1/admin/** 에서 이미 롤 체크됨)
                        .requestMatchers("/api/v1/refund-requests/**").authenticated()
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
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        // 👇 이 세 줄을 추가하세요! (실패 시 프론트엔드로 얌전하게 돌려보냄)
                        .failureHandler((request, response, exception) -> {
                            // 실패 원인을 로그로 찍어보면 디버깅하기 좋습니다.
                            System.out.println("소셜 로그인 실패 원인: " + exception.getMessage());

                            // 프론트엔드의 로그인 페이지로 에러 표시와 함께 돌려보냅니다.
                            response.sendRedirect("http://localhost:17000/login?error=true");
                        })
                )
                .addFilterBefore(globalJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🌟 2. CORS 상세 정책 빈 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🚨 프론트엔드 도메인을 정확히 명시해야 브라우저가 허용합니다 (와일드카드 * 금지)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:17000",
                "http://127.0.0.1:17000",
                "https://kidmily.kro.kr",
                "https://algoga.kro.kr",
                "https://*.vercel.app", // '프로젝트명' 자리에 무엇이 오든 허용
                "https://www.kidmily.kro.kr"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // 🌟 프론트엔드와 쿠키(인증 정보) 통신을 위해 반드시 true
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "X-Trace-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
