package com.kidmily.algoga_server.user.settings;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey key; // 0.12.x 부터는 Key 대신 SecretKey 사용 권장

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String email) {
        return Jwts.builder()
                .subject(email) // 0.12.x 문법 (setSubject -> subject)
                .issuedAt(new Date()) // setIssuedAt -> issuedAt
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // setExpiration -> expiration
                .signWith(key) // signWith(key, 알고리즘) -> signWith(key) 로 자동 인식
                .compact();
    }

    public String createRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser() // parserBuilder() -> parser()
                .verifyWith(key) // setSigningKey() -> verifyWith()
                .build()
                .parseSignedClaims(token) // parseClaimsJws() -> parseSignedClaims()
                .getPayload() // getBody() -> getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}