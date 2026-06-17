package com.kidmily.algoga_server.global.security;

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

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalJwtAuthenticationFilter extends OncePerRequestFilter {

    private final GlobalJwtProvider globalJwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate; // 🌟 블랙리스트 검증용 Redis 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && globalJwtProvider.validateToken(token)) {
            try {
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

                    // 🌟 블랙리스트 등록된 유저인지 Redis 검증 (Access Token 무효화)
                    String isBlacklisted = redisTemplate.opsForValue().get("BLACKLIST:" + email);
                    if ("true".equals(isBlacklisted)) {
                        log.warn("블랙리스트 유저의 비정상적 API 접근 시도 차단: {}", email);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
            } catch (Exception e) {
                log.error("JWT 인증 처리 중 에러 발생: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    // 🌟 헤더 대신 쿠키에서 accessToken을 찾도록 수정
    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}