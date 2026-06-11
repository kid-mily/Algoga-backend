package com.kidmily.algoga_server.benefit.application.port;

import java.util.Optional;

public interface UserProfilePort {

    Optional<UserProfile> findProfile(Long userId);

    default boolean exists(Long userId) {
        return findProfile(userId).isPresent();
    }

    record UserProfile(
            Long userId,
            String name,
            String email
    ) {
    }
}
