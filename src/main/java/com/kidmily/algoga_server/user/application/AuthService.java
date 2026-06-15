package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.event.UserSignedUpEvent;
import com.kidmily.algoga_server.global.infrastructure.mail.EmailSender;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.global.security.dto.SocialAuthResult;
import com.kidmily.algoga_server.global.security.port.SocialLoginProcessor;
import com.kidmily.algoga_server.user.domain.Gender;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.*;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.FindIdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements SocialLoginProcessor {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalJwtProvider globalJwtProvider;
    private final EmailSender emailSender;
    private final RedisTemplate<String, String> redisTemplate; // Redis 도구 주입!
    private final ApplicationEventPublisher eventPublisher;

    // 프론트엔드 주소 주입 (HTTP cookie할 때 추가함)
    @Value("${app.frontend.base-url:http://localhost:17000}")
    private String frontendBaseUrl;

    // 이메일 인증번호 발송
    public void sendVerificationCode(SendEmailCodeRequest request) {
        String email = request.email().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 6자리 랜덤 난수 생성
        String code = String.valueOf((int) (Math.random() * 899999) + 100000);

        // Redis에 저장 (키: "AUTH_CODE:이메일", 값: 인증번호, 만료시간: 3분)
        redisTemplate.opsForValue().set("AUTH_CODE:" + email, code, 3, TimeUnit.MINUTES);

        // 이메일 발송
        String subject = "[ALGOGA] 회원가입 이메일 인증번호";
        String body = "안녕하세요, ALGOGA입니다.\n\n"
                + "요청하신 회원가입 인증번호는 다음과 같습니다.\n"
                + "인증번호 : [" + code + "]\n\n"
                + "3분 이내에 입력해 주세요.";

        emailSender.sendEmail(email, subject, body);
        log.info("회원가입 인증번호 발송 완료 [요청 이메일: {}]", email);
    }

    // 이메일 인증번호 확인
    public void verifyEmailCode(VerifyEmailCodeRequest request) {
        String email = request.email().toLowerCase();

        // 1. Redis에서 해당 이메일의 인증번호 꺼내기
        String savedCode = redisTemplate.opsForValue().get("AUTH_CODE:" + email);

        // 2. 검증 (포스트잇이 없거나, 번호가 다르면 에러!)
        if (savedCode == null || !savedCode.equals(request.code())) {
            throw new AuthException(AuthErrorCode.EMAIL_AUTH_CODE_MISMATCH);
        }

        // 3. 인증 성공 시: 기존 포스트잇 떼서 버리고, "인증 완료" 포스트잇을 30분짜리로 새로 붙임
        redisTemplate.delete("AUTH_CODE:" + email);
        redisTemplate.opsForValue().set("AUTH_SUCCESS:" + email, "true", 30, TimeUnit.MINUTES);

        log.info("이메일 인증 성공 [이메일: {}]", email);
    }

    // 회원가입
    public void signup(AuthSignupRequest request) {
        String email = request.email().toLowerCase();

        // 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 아이디(username) 중복 검사
        if (userRepository.existsByUsername(request.username())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }

        // 이메일 인증
        String isVerified = redisTemplate.opsForValue().get("AUTH_SUCCESS:" + email);
        if (isVerified == null || !isVerified.equals("true")) {
            throw new AuthException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = User.builder()
                .username(request.username())
                .email(email)
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
                .termsServiceAgreed(request.termsServiceAgreed())
                .termsPrivacyAgreed(request.termsPrivacyAgreed())
                .termsMarketingAgreed(request.termsMarketingAgreed())
                .build();
        User savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserSignedUpEvent(savedUser.getId()));

        // 가입이 성공적으로 끝났으니, "인증 완료" 포스트잇도 떼서 버립니다! (청소)
        redisTemplate.delete("AUTH_SUCCESS:" + email);

        log.info("신규 회원가입 완료 [아이디: {}, 이메일: {}]", user.getUsername(), user.getEmail());
    }

    // 아이디 중복 확인 로직
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    // 4. 일반 로그인
    public AuthTokenResponse login(AuthLoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (user.isDeleted()) throw new UserException(UserErrorCode.DELETED_USER);
        if (user.isAccountLocked()) {
            log.warn("잠긴 계정에 로그인 시도 발생 [아이디: {}]", user.getUsername());
            throw new UserException(UserErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            user.increaseLoginFailure();
            userRepository.save(user);

            if (user.isAccountLocked()) {
                log.warn("비밀번호 5회 연속 오류로 계정 잠금 처리됨 [아이디: {}]", user.getUsername());
            } else {
                log.warn("비밀번호 입력 오류 [아이디: {}, 누적 실패: {}/5]", user.getUsername(), user.getLoginFailCount());
            }
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        user.resetLoginFailure();
        userRepository.save(user);

        String accessToken = globalJwtProvider.createUserAccessToken(user.getEmail());
        String refreshToken = globalJwtProvider.createUserRefreshToken(user.getEmail());

        // Redis에 Refresh Token 저장 (만료시간 7일)
        redisTemplate.opsForValue().set(
                "RT:" + user.getEmail(),
                refreshToken,
                604800000,
                TimeUnit.MILLISECONDS
        );

        log.info("로그인 성공 [아이디: {}]", user.getUsername());

        return new AuthTokenResponse(accessToken, refreshToken, user.getRequiresPasswordChange());
    }

    // 5. 아이디 찾기
    public FindIdResponse findId(FindIdRequest request) {
        User user = userRepository.findByNameAndEmail(request.name(), request.email().toLowerCase())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String username = user.getUsername();
        String maskedId = maskId(username);

        log.info("아이디 찾기 완료 [요청 이메일: {}, 마스킹된 아이디 반환: {}]", request.email(), maskedId);

        return new FindIdResponse(maskedId);
    }

    // 6. 비밀번호 찾기
    public void findPassword(FindPasswordRequest request) {
        User user = userRepository.findByUsernameAndEmail(request.username(), request.email().toLowerCase())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "1a";
        user.setTemporaryPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        String subject = "[ALGOGA] 임시 비밀번호 발급 안내";
        String body = "안녕하세요, ALGOGA입니다.\n\n"
                + "요청하신 임시 비밀번호는 다음과 같습니다.\n"
                + "임시 비밀번호 : " + tempPassword + "\n\n"
                + "로그인 후 반드시 비밀번호를 변경해 주세요.";

        emailSender.sendEmail(user.getEmail(), subject, body);

        log.info("임시 비밀번호 발급 및 메일 전송 완료. [요청 이메일: {}]", user.getEmail());
    }

    private String maskId(String id) {
        if (id == null || id.length() < 3) return id;
        return id.substring(0, 3) + "*".repeat(id.length() - 3);
    }

    // 7. 비밀번호 강제 변경
    public void resetPassword(String email, ResetPasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (!user.getRequiresPasswordChange()) {
            log.warn("비정상적인 비밀번호 강제 변경 시도 탐지 (변경 대상이 아님) [이메일: {}]", email);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        user.changePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 강제 변경 완료 [이메일: {}]", email);
    }

    // 8. 로그아웃
    public void logout(String email) {
        // Redis에서 해당 유저의 Refresh Token 삭제
        redisTemplate.delete("RT:" + email);
        log.info("로그아웃 처리 완료 [접속 종료 이메일: {}]", email);
    }

    // 토큰 재발급을 위한 검증 메서드
    public String refreshAccessToken(String email, String refreshToken) {
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        return globalJwtProvider.createUserAccessToken(email);
    }

    // 소셜 전용 추가정보 회원가입
    public void socialSignup(AuthSocialSignupRequest request) {
        String email = request.email().toLowerCase();

        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 요구사항 명세 반영: 아이디(username)는 이메일로 대체
        String username = email;

        // 3. 요구사항 명세 반영: 소셜 유저용 더미(랜덤) 비밀번호 생성
        String dummyPassword = UUID.randomUUID().toString();

        // 4. 유저 엔티티 생성 및 저장
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(dummyPassword)) // 난수 암호화 저장
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .gender(Gender.valueOf(request.gender().toUpperCase()))
                .nickname(request.nickname())
                .socialType(SocialType.valueOf(request.socialType().toUpperCase()))
                .personalCode(UUID.randomUUID().toString())
                .loginFailCount(0)
                .isDeleted(false)
                .referralCode(request.referralCode())
                .signupPath(request.signupPath())
                .createdAt(LocalDateTime.now())
                .requiresPasswordChange(false)
                .termsServiceAgreed(request.termsServiceAgreed())
                .termsPrivacyAgreed(request.termsPrivacyAgreed())
                .termsMarketingAgreed(request.termsMarketingAgreed())
                .build();

        User savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserSignedUpEvent(savedUser.getId()));

        log.info("소셜 신규 회원가입 완료 [아이디(이메일): {}, 소셜: {}]",
                user.getUsername(), user.getSocialType());
    }

    // 🌟 소셜로그인 리다이렉트 및 토큰 발급 (최신 쿠키 전용 버전)
    @Override
    public SocialAuthResult processLoginAndGetRedirectUrl(String email, String name) {
        // 이미 가입된 유저인지 DB 확인
        boolean isExistingUser = userRepository.findByEmailAndIsDeletedFalse(email).isPresent();

        if (isExistingUser) {
            String accessToken = globalJwtProvider.createUserAccessToken(email);
            String refreshToken = globalJwtProvider.createUserRefreshToken(email);

            // 기존 일반 로그인과 똑같이 7일간 Redis에 저장
            redisTemplate.opsForValue().set("RT:" + email, refreshToken, 604800000, TimeUnit.MILLISECONDS);
            log.info("소셜 로그인 성공 (기존 유저) [이메일: {}]", email);

            // URL 파라미터 전부 제거! (토큰은 쿠키로 구울 거니까)
            String redirectUrl = frontendBaseUrl + "/auth/oauth-callback";
            return new SocialAuthResult(redirectUrl, accessToken, refreshToken);
        } else {
            log.info("소셜 로그인 (신규 유저 발견, 회원가입 유도) [이메일: {}]", email);

            String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/auth/register")
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .queryParam("socialType", "GOOGLE")
                    .build().toUriString();
            return new SocialAuthResult(redirectUrl, null, null);
        }
    }
}
}
