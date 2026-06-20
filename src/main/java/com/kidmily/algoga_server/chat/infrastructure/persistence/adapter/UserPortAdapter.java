package com.kidmily.algoga_server.chat.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.chat.application.port.UserPort;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("chatUserPortAdapter")
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
    public Optional<Long> findUserIdById(Long userId) {
        return userRepository.findById(userId).map(User::getId);
    }
}