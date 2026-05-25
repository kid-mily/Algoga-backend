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

        if (token != null && adminJwtProvider.validateToken(token)) {
            // [기존 코드] 정상적인 토큰일 경우 데이터 추출 및 인증
            Long id = adminJwtProvider.getId(token);
            String loginId = adminJwtProvider.getSubject(token);
            String role = adminJwtProvider.getRole(token);

            CustomManagerDetails managerDetails = new CustomManagerDetails(id, loginId, role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    managerDetails, null, managerDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            // 🚨 [테스트용 임시 코드 시작] 토큰이 없거나 틀려도 무조건 강제 통과!
            System.out.println("⚠️ [TEST MODE] 인증 없이 무적 권한(SUPER_ADMIN)으로 강제 통과시킵니다.");

            // PK 1번, 아이디 test_admin, 권한 SUPER_ADMIN을 가진 가짜 매니저 객체 생성
            CustomManagerDetails dummyManager = new CustomManagerDetails(1L, "test_admin", "ROLE_SUPER_ADMIN");

            UsernamePasswordAuthenticationToken dummyAuth = new UsernamePasswordAuthenticationToken(
                    dummyManager, null, dummyManager.getAuthorities()
            );
            // 시큐리티에 가짜 신분증 제출
            SecurityContextHolder.getContext().setAuthentication(dummyAuth);
            // 🚨 [테스트용 임시 코드 끝]
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 🌟 추가: 어드민 API가 아닌 요청(유저 요청 등)은 이 필터를 무시하고 패스하도록 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/admin");
    }
}