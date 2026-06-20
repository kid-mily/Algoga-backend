package com.kidmily.algoga_server.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.admin.settings.CustomManagerDetails;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import com.kidmily.algoga_server.user.settings.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalJwtAuthenticationFilter extends OncePerRequestFilter {

    private final GlobalJwtProvider globalJwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

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
                    String isBlacklisted = redisTemplate.opsForValue().get("BLACKLIST:" + email);
                    if ("true".equals(isBlacklisted)) {
                        log.warn("블랙리스트 유저의 비정상적 API 접근 시도 차단: {}", email);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":\"AUTH_015\",\"message\":\"블랙리스트에 등록되어 접근이 영구히 제한된 계정입니다. 고객센터에 문의하세요.\"}");
                        return;
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