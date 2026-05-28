package com.kidmily.algoga_server.global.security;

import com.kidmily.algoga_server.admin.settings.CustomManagerDetails;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import com.kidmily.algoga_server.user.settings.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalJwtAuthenticationFilter extends OncePerRequestFilter {

    private final GlobalJwtProvider globalJwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 존재하고 유효성 검증을 통과한 경우
        if (token != null && globalJwtProvider.validateToken(token)) {
            try {
                // 🌟 여기서 토큰의 타입을 확인하여 분기 처리합니다.
                String type = globalJwtProvider.getType(token);

                if ("ADMIN".equals(type)) {
                    // [ADMIN 권한 부여 로직]
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
                    // [USER 권한 부여 로직] (DB 조회 후 세팅)
                    String email = globalJwtProvider.getSubject(token);
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

        // 경로 기반의 skip 로직(shouldNotFilter)은 SecurityConfig에 위임하여 제거합니다.
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}