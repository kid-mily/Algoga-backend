package com.kidmily.algoga_server.course.application.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserProfilePort {

    Optional<UserProfile> findProfile(Long userId);

    Map<Long, UserProfile> findProfiles(Collection<Long> userIds);

    void updateDiagnosisResult(Long userId, Long countryId, String level, Integer score);

    record UserProfile(
            Long userId,
            String username,
            String name,
            String email,
            String nickname
    ) {
    }
}