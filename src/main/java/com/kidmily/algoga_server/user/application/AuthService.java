package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.user.domain.Gender;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.AuthLoginRequest;
import com.kidmily.algoga_server.user.presentation.request.AuthSignupRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.settings.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 1. 일반 회원가입
    public void signup(AuthSignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(UserErrorCode.ALREADY_EXISTS_EMAIL);
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .gender(Gender.valueOf(request.gender().toUpperCase()))
                .nickname(request.nickname())
                .socialType(SocialType.LOCAL)
                .personalCode(UUID.randomUUID().toString())
                .loginFailCount(0)
                .isDeleted(false)
                .referralCode(request.referralCode())
                .signupPath(request.signupPath())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    // 2. 일반 로그인 (Redis 없이 JWT 반환)
    public AuthTokenResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 탈퇴한 유저인지 확인
        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        // 5회 이상 틀려서 잠긴 유저인지 확인
        if (user.isAccountLocked()) {
            throw new UserException(UserErrorCode.ACCOUNT_LOCKED);
        }

        // 비밀번호 불일치 시
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            user.increaseLoginFailure();
            userRepository.save(user); // 틀린 횟수 DB에 즉시 저장
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        // 로그인 성공 시 실패 카운트 초기화
        user.resetLoginFailure();
        userRepository.save(user);

        // JWT 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // Redis 저장 없이 바로 프론트엔드로 전달
        return new AuthTokenResponse(accessToken, refreshToken);
    }
}