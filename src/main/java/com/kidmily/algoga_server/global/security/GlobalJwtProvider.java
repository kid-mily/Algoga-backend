package com.kidmily.algoga_server.global.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class GlobalJwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ==========================================
    // 1. 일반 유저(USER) 토큰 발급
    // ==========================================
    public String createUserAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("type", "USER") // 🌟 유저 타입
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String createUserRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public ResponseCookie createCookie(String key, String token) {
        return ResponseCookie.from(key, token)
                .httpOnly(true)
                .secure(true)           // 배포 시 true, 로컬 개발 시 false
                .path("/")
                .domain(".kidmily.kro.kr")  // 🌟 상위 도메인 적용
                .sameSite("None")       // 🌟 크로스 도메인 허용
                .maxAge(604800000)      // 필요한 만료 시간
                .build();
    }

    public ResponseCookie deleteCookie(String key) {
        return ResponseCookie.from(key, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(".kidmily.kro.kr") // 🌟 생성 시 사용한 도메인과 완벽하게 일치시킴
                .sameSite("None")
                .maxAge(0)                // 🌟 수명을 0으로 주어 즉시 삭제 유도
                .build();
    }

    // ==========================================
    // 2. 어드민(ADMIN) 토큰 발급
    // ==========================================
    public String createAdminAccessToken(Long managerId, String loginId, String role) {
        return Jwts.builder()
                .subject(loginId)
                .claim("type", "ADMIN") // 🌟 어드민 타입
                .claim("id", managerId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String createAdminRefreshToken(String loginId) {
        return Jwts.builder()
                .subject(loginId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    // ==========================================
    // 3. 공통 토큰 검증 및 정보 추출
    // ==========================================
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getType(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("type", String.class);
    }

    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public Date getExpiration(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getExpiration();
    }

    // 어드민 전용 클레임 추출
    public Long getAdminId(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    public String getAdminRole(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
    }
}