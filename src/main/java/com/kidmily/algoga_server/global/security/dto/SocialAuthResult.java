package com.kidmily.algoga_server.global.security.dto;

public record SocialAuthResult(
        String redirectUrl,
        String accessToken,
        String refreshToken
) {}