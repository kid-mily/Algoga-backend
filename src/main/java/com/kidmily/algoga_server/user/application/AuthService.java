package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.user.domain.Gender;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.*;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.FindIdResponse;
import com.kidmily.algoga_server.user.settings.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 1. 회원가입
    public void signup(AuthSignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(UserErrorCode.ALREADY_EXISTS_EMAIL);
        }
        User user = User.builder()
                .username(request.username())
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
                .requiresPasswordChange(false)
                .build();
        userRepository.save(user);
    }

    // 2. 로그인
    public AuthTokenResponse login(AuthLoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (user.isDeleted()) throw new UserException(UserErrorCode.DELETED_USER);
        if (user.isAccountLocked()) throw new UserException(UserErrorCode.ACCOUNT_LOCKED);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            user.increaseLoginFailure();
            userRepository.save(user);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }
        user.resetLoginFailure();
        userRepository.save(user);

        String accessToken = jwtProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // ⭐️ 여기서 user.getRequiresPasswordChange()를 넘겨줍니다.
        return new AuthTokenResponse(accessToken, refreshToken, user.getRequiresPasswordChange());
    }

    // 3. 아이디 찾기 (아이디를 가져와서 마스킹하도록 수정)
    public FindIdResponse findId(FindIdRequest request) {
        User user = userRepository.findByNameAndEmail(request.name(), request.email().toLowerCase())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String username = user.getUsername();

        return new FindIdResponse(maskId(username));
    }

    // 4. 비밀번호 찾기
    public void findPassword(FindPasswordRequest request) {
        // ⭐️ findByNameAndEmail -> findByUsernameAndEmail로 변경
        User user = userRepository.findByUsernameAndEmail(request.username(), request.email().toLowerCase())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "1a";
        user.setTemporaryPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        log.info("임시 비밀번호가 발급되었습니다. [이메일: {}, 임시 비밀번호: {}]", user.getEmail(), tempPassword);
    }

    private String maskId(String id) {
        if (id == null || id.length() < 3) return id;
        return id.substring(0, 3) + "*".repeat(id.length() - 3);
    }

    // 5. 비밀번호 강제 변경 (임시 비밀번호로 로그인한 유저 대상)
    public void resetPassword(String email, ResetPasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 만약 임시 비밀번호로 로그인한 상태가 아니라면 에러 발생! (보안 방어)
        if (!user.getRequiresPasswordChange()) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD); // "정상적인 변경 요청이 아닙니다" 등으로 에러코드 추가해도 좋음!
        }
        // 1. 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(request.newPassword());

        // 2. User 엔티티의 changePassword 메서드 호출 (비번 변경 + 플래그 false 처리)
        user.changePassword(encodedNewPassword);

        userRepository.save(user); // 3. 저장

        log.info("비밀번호 강제 변경 완료 [이메일: {}]", email);
    }

    // 6. 로그아웃
    public void logout(String email) {
        // 현재 Redis를 사용하지 않으므로, 백엔드에서는 로그만 남깁니다.
        // (실제 토큰 무효화는 클라이언트가 localStorage에서 토큰을 지우는 것으로 완료됩니다.)
        log.info("로그아웃 처리 완료 [접속 종료 이메일: {}]", email);
    }



}