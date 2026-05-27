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
                .map(User::getNickname)
                .orElse("알 수 없음");
    }

    @Override
    public String getProfileImageUrl(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfileImageUrl)
                .orElse(null);
    }
}