package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.application.port.UserPort;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {

    private final UserRepository userRepository;

    @Override
    public String getNickname(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.isDeleted() ? "탈퇴한 사용자" : user.getNickname())
                .orElse("탈퇴한 사용자");  // 하드딜리트된 경우도 동일하게
    }

    @Override
    public String getProfileImageUrl(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .map(User::getProfileImageUrl)
                .orElse(null);
    }
}