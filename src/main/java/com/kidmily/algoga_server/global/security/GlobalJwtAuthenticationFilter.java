package com.kidmily.algoga_server.global.security;

import com.kidmily.algoga_server.admin.settings.CustomManagerDetails;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import com.kidmily.algoga_server.user.settings.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.kidmily.algoga_server.global.util.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalJwtAuthenticationFilter extends OncePerRequestFilter {

    private final GlobalJwtProvider globalJwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // 로그인/가입/토큰재발급처럼 "기존 세션 상태와 무관하게 항상 동작해야 하는" 엔드포인트 목록.
    // 브라우저에 예전 세션의 낡은(만료됐거나 다른 기기 로그인으로 무효화된) accessToken 쿠키가 남아있으면
    // 이 필터가 실제 로직 실행 전에 그 쿠키부터 검증하다가 401로 막아버리는 문제가 있었다.
    // (신규 로그인 시도 자체가 예전 쿠키 때문에 "다른 기기에서 로그인됨"으로 거부되는 버그의 원인)
    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/social/signup",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/find-id",
            "/api/v1/auth/find-password",
            "/api/v1/auth/username/check",
            "/api/v1/auth/phone/check",
            "/api/v1/auth/email/send-code",
            "/api/v1/auth/email/verify-code"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_AUTH_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && globalJwtProvider.validateToken(token)) {
                String type = globalJwtProvider.getType(token);

                if ("ADMIN".equals(type)) {
                    Long id = globalJwtProvider.getAdminId(token);
                    String loginId = globalJwtProvider.getSubject(token);
                    String role = globalJwtProvider.getAdminRole(token);

                    CustomManagerDetails managerDetails = new CustomManagerDetails(id, loginId, role);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            managerDetails, null, managerDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else if ("USER".equals(type)) {
                    String email = globalJwtProvider.getSubject(token);

                    // 블랙리스트 검증 로직
                    String isBlacklisted = redisTemplate.opsForValue().get(RedisKeys.BLACKLIST_PREFIX + email);
                    if ("true".equals(isBlacklisted)) {
                        log.warn("블랙리스트 유저의 비정상적 API 접근 시도 차단: {}", email);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(String.format(
                                "{\"code\":\"%s\",\"message\":\"%s\"}",
                                AuthErrorCode.BLACKLISTED_USER.getCode(), AuthErrorCode.BLACKLISTED_USER.getMessage()
                        ));
                        return;
                    }

                    // 이중 로그인(중복 로그인) 검증: 다른 기기에서 새로 로그인해서 활성 세션이 바뀌었으면 이 토큰은 더 이상 유효한 세션이 아님
                    String activeAccessToken = redisTemplate.opsForValue().get(RedisKeys.ACTIVE_AT_PREFIX + email);
                    if (activeAccessToken != null && !activeAccessToken.equals(token)) {
                        log.warn("다른 기기에서 로그인되어 종료된 세션의 접근 차단: {}", email);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(String.format(
                                "{\"code\":\"%s\",\"message\":\"%s\"}",
                                AuthErrorCode.DUPLICATE_LOGIN.getCode(), AuthErrorCode.DUPLICATE_LOGIN.getMessage()
                        ));
                        return;
                    }

                    // 🌟 요청이 들어왔다 = 활동 중이다 -> idle 타임아웃(30분) 타이머를 다시 밀어줌 (sliding expiration)
                    if (activeAccessToken != null) {
                        redisTemplate.expire(RedisKeys.ACTIVE_AT_PREFIX + email, accessTokenExpiration, TimeUnit.MILLISECONDS);
                    }

                    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료 에러 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"AUTH_EXPIRED\",\"message\":\"토큰이 만료되었습니다. 다시 로그인해주세요.\"}");
            return;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰 에러 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"AUTH_INVALID\",\"message\":\"유효하지 않은 토큰입니다.\"}");
            return;

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 서버 에러 발생: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"SYS_ERROR\",\"message\":\"인증 처리 중 서버 에러가 발생했습니다.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // 🌟 핵심 변경 포인트: API 요청 경로에 따라 읽어올 쿠키 이름을 동적으로 결정합니다.
    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        String requestURI = request.getRequestURI();

        // 1. 관리자(Admin) 전용 API 요청인 경우 -> 'adminAccessToken' 쿠키 탐색
        if (requestURI.contains("/admin")) {
            for (Cookie cookie : request.getCookies()) {
                if ("adminAccessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 2. 그 외 일반 유저 API 요청인 경우 -> 'accessToken' 쿠키 탐색
        else {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}