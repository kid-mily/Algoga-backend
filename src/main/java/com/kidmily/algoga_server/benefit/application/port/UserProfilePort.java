package com.kidmily.algoga_server.benefit.application.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserProfilePort {

    Optional<UserProfile> findProfile(Long userId);

    Map<Long, UserProfile> findProfiles(Collection<Long> userIds);

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
