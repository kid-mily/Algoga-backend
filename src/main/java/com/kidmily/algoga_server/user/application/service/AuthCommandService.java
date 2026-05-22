package com.kidmily.algoga_server.user.application.service;

import com.kidmily.algoga_server.user.application.command.AuthLoginCommand;
import com.kidmily.algoga_server.user.application.command.AuthSignupCommand;
import com.kidmily.algoga_server.user.application.usecase.AuthCommandUseCase;
import com.kidmily.algoga_server.user.domain.model.User;
import com.kidmily.algoga_server.user.domain.repository.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.user.presentation.api.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.settings.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService implements AuthCommandUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public void signup(AuthSignupCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException(UserErrorCode.ALREADY_EXISTS_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                command.email(),
                encodedPassword,
                command.name(),
                command.phone(),
                command.birthDate(),
                command.gender(),
                command.nickname()
        );

        userRepository.save(user);
    }

    @Override
    public AuthTokenResponse login(AuthLoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            user.increaseLoginFailCount();
            userRepository.save(user); // 실패 횟수 저장
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        user.resetLoginFailCount();
        userRepository.save(user);

        // JWT 토큰 생성 (Subject를 이메일로 지정)
        String accessToken = jwtProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // TODO: Redis에 RefreshToken 저장 로직 추가 (추후 작업)

        return new AuthTokenResponse(accessToken, refreshToken);
    }
}