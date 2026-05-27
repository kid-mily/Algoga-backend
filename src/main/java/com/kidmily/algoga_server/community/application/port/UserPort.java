package com.kidmily.algoga_server.community.application.port;

public interface UserPort {
    String getNickname(Long userId);
    String getProfileImageUrl(Long userId);
}