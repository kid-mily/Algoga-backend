package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.kidmily.algoga_server.calendar.application.port.UserPort;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("calendarUserPortAdapter")
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {

    private final UserRepository userRepository;

    @Override
    public String getUserEmail(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElse(null);
    }

    @Override
    public String getUserName(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getName())
                .orElse(null);
    }
}