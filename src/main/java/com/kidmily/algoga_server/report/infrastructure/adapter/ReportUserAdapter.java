package com.kidmily.algoga_server.report.infrastructure.adapter;

import com.kidmily.algoga_server.report.application.port.ReportUserPort;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ReportUserAdapter implements ReportUserPort {

    private final UserRepository userRepository;

    @Override
    public String getNickname(Long userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("알 수 없음");
    }

    @Override
    public List<Long> findUserIdsByNicknameContaining(String keyword) {
        List<User> byNickname = userRepository.findByNicknameContaining(keyword);
        List<User> byName = userRepository.findByNameContaining(keyword);

        return Stream.concat(byNickname.stream(), byName.stream())
                .map(User::getId)
                .distinct()
                .toList();
    }
}