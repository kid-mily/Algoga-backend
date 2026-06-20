package com.kidmily.algoga_server.global.websocket;

import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketUserPortAdapter implements WebSocketUserPort {

    private final UserRepository userRepository;
    
    public Optional<Long> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getId);
    }
}