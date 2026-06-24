package com.kidmily.algoga_server.chat.application.port;

import java.util.Optional;

public interface UserPort {
    String getNickname(Long userId);
    String getProfileImageUrl(Long userId);
    Optional<Long> findUserIdById(Long userId);
}