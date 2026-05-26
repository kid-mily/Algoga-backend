package com.kidmily.algoga_server.user.settings;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 🌟 로그 출력을 위해 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j // 🌟 어노테이션 추가
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            String email = jwtProvider.getSubject(token);

            try {
                // 🌟 예외가 발생할 수 있는 부분을 try-catch로 감쌉니다.
                CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 🔥 일반 유저 DB에 계정이 없는 경우 (예: 관리자 토큰인 경우)
                // 서버를 터뜨리지 않고 로그만 가볍게 남긴 뒤 다음 필터로 조용히 넘깁니다.
                log.debug("일반 유저 계정이 아닙니다. (관리자 요청으로 간주하고 패스합니다) - {}", e.getMessage());
            }
        }

        // 🌟 에러가 나더라도 무조건 다음 필터 체인(어드민 필터나 컨트롤러)으로 넘어가게 됨
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