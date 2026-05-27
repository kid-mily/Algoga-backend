package com.kidmily.algoga_server.admin.settings;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminJwtProvider adminJwtProvider; // 어드민 전용 프로바이더 사용

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 🌟 토큰이 있고, 타입이 "ADMIN"일 때만 어드민 인증 로직 수행
        if (token != null && adminJwtProvider.validateToken(token)) {
            if ("ADMIN".equals(adminJwtProvider.getType(token))) {
                Long id = adminJwtProvider.getId(token);
                String loginId = adminJwtProvider.getSubject(token);
                String role = adminJwtProvider.getRole(token);

                CustomManagerDetails managerDetails = new CustomManagerDetails(id, loginId, role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        managerDetails, null, managerDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // (테스트용 강제 통과 로직은 필요에 따라 유지/삭제)

        filterChain.doFilter(request, response);
    }

    // 이미 잘 작성되어 있는 부분 (유지)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/admin");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}