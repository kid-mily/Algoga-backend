package com.kidmily.algoga_server.course.application.port;

import java.util.Optional;

public interface UserProfilePort {

    Optional<UserProfile> findProfile(Long userId);

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