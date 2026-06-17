package com.kidmily.algoga_server.notice.infrastructure.persistence;

import com.kidmily.algoga_server.notice.application.port.UserPort;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("noticeUserPortAdapter")
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {
    private final UserRepository userRepository;

    @Override
    public List<Long> findAllActiveUserIds() {
        return userRepository.findAllActiveUserIds();
    }
}