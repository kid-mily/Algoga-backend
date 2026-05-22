package com.kidmily.algoga_server.user.settings.security.jwt;

import com.kidmily.algoga_server.user.settings.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private final SecretKey secretKey;

    public JwtProvider(JwtProperties jwtProperties) {

        this.jwtProperties = jwtProperties;

        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecretKey().getBytes()
        );
    }

    public String createAccessToken(Long userId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + jwtProperties.getAccessTokenExpiration()
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + jwtProperties.getRefreshTokenExpiration()
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {

        Claims claims = parseClaims(token);

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {

        try {

            parseClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}