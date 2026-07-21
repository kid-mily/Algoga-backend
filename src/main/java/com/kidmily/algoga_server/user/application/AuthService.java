package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.event.UserSignedUpEvent;
import com.kidmily.algoga_server.global.exception.AccountLockedException;
import com.kidmily.algoga_server.global.exception.InvalidPasswordException;
import com.kidmily.algoga_server.global.infrastructure.mail.EmailSender;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.global.security.dto.SocialAuthResult;
import com.kidmily.algoga_server.global.security.port.SocialLoginProcessor;
import com.kidmily.algoga_server.global.util.RedisKeys;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements SocialLoginProcessor {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalJwtProvider globalJwtProvider;
    private final EmailSender emailSender;
    private final RedisTemplate<String, String> redisTemplate; // Redis 도구 주입!
    private final ApplicationEventPublisher eventPublisher;
    private final EmailVerificationHelper emailVerificationHelper;

    // 프론트엔드 주소 주입 (HTTP cookie할 때 추가함)
    @Value("${user.app.frontend.base-url}")
    private String frontendBaseUrl;

    // 이중 로그인 방지용 "현재 활성 AccessToken" 만료시간 (AccessToken 자체 만료시간과 동일하게 맞춤)
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // 이메일 인증번호 발송
    public void sendVerificationCode(SendEmailCodeRequest request) {
        String email = request.email().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 탈퇴 후 30일 이내에 같은 이메일로 재가입 시도하는지 확인
        if (redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + email)) {
            throw new AuthException(AuthErrorCode.RECENTLY_WITHDRAWN_EMAIL);
        }

        emailVerificationHelper.sendCode(
                email,
                RedisKeys.AUTH_CODE_PREFIX,
                "[ALGOGA] 회원가입 이메일 인증번호",
                "안녕하세요, ALGOGA입니다.\n\n요청하신 회원가입 인증번호는 다음과 같습니다."
        );
        log.info("회원가입 인증번호 발송 완료 [요청 이메일: {}]", email);
    }

    // 이메일 인증번호 확인
    public void verifyEmailCode(VerifyEmailCodeRequest request) {
        String email = request.email().toLowerCase();

        emailVerificationHelper.verifyCode(email, request.code(), RedisKeys.AUTH_CODE_PREFIX, RedisKeys.AUTH_SUCCESS_PREFIX);

        log.info("이메일 인증 성공 [이메일: {}]", email);
    }

    // 회원가입
    @Transactional
    public void signup(AuthSignupRequest request) {
        String email = request.email().toLowerCase();

        // 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 탈퇴 후 30일 이내에 같은 이메일로 재가입 시도하는지 확인
        if (redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + email)) {
            throw new AuthException(AuthErrorCode.RECENTLY_WITHDRAWN_EMAIL);
        }

        // 아이디(username) 중복 검사
        if (userRepository.existsByUsername(request.username())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }

        // 전화번호 중복 검사 추가
        if (userRepository.existsByPhone(request. phone())) {
            throw new UserException(UserErrorCode.ALREADY_EXISTS_PHONE);
        }

        // 이메일 인증
        String isVerified = redisTemplate.opsForValue().get(RedisKeys.AUTH_SUCCESS_PREFIX + email);
        if (isVerified == null || !isVerified.equals("true")) {
            throw new AuthException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }

        Long referrerUserId = resolveReferrerUserId(request.referralCode());

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
//                .personalCode(UUID.randomUUID().toString())
                .personalCode(generateUniquePersonalCode()) // 6자리 난수 생성 메서드 호출
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
        eventPublisher.publishEvent(new UserSignedUpEvent(
                savedUser.getId(),
                referrerUserId,
                normalizeReferralCode(request.referralCode())
        ));

        // 가입이 성공적으로 끝났으니, "인증 완료" 포스트잇도 떼서 버립니다! (청소)
        redisTemplate.delete(RedisKeys.AUTH_SUCCESS_PREFIX + email);

        log.info("신규 회원가입 완료 [아이디: {}, 이메일: {}]", user.getUsername(), user.getEmail());
    }

    // 아이디 중복 확인 로직
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    // 전화번호 중복 확인 로직 (회원가입 1단계에서 실시간 확인용)
    // 정규화 없이 문자열 그대로 비교한다 -> 가입 시 저장되는 형식과 100% 동일한 기준으로 검사되도록 함
    @Transactional(readOnly = true)
    public boolean isPhoneAvailable(String phone) {
        return !userRepository.existsByPhone(phone);
    }

    // 4. 일반 로그인 (로그인 병목 최적화 적용 (트랜잭션 분리))
    public AuthTokenResponse login(AuthLoginRequest request) {
        // [1] DB 조회 (별도 트랜잭션 호출)
        User user = findUserByUsername(request.username());

        if (user.isDeleted()) throw new UserException(UserErrorCode.DELETED_USER);
        if (user.isAccountLocked()) {
            long remainingSeconds = Duration.between(LocalDateTime.now(), user.getLockedUntil()).getSeconds();
            log.warn("잠긴 계정에 로그인 시도 발생 [아이디: {}, 남은 시간: {}초]", user.getUsername(), remainingSeconds);
            throw new AccountLockedException(UserErrorCode.ACCOUNT_LOCKED, remainingSeconds);
        }

        // 블랙리스트 여부 확인
        String isBlacklisted = redisTemplate.opsForValue().get(RedisKeys.BLACKLIST_PREFIX + user.getEmail());
        if ("true".equals(isBlacklisted)) {
            log.warn("블랙리스트 유저의 로그인 시도 차단 [아이디: {}]", user.getUsername());
            throw new AuthException(AuthErrorCode.BLACKLISTED_USER);
        }

        // [2] BCrypt 연산 (트랜잭션 밖에서 실행 - 커넥션 점유 X)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            updateLoginFailure(user); // [분리된 수정 트랜잭션 호출]

            if (user.isAccountLocked()) {
                // 이번 시도로 막 잠긴 경우: "비밀번호 틀림" 대신 바로 "계정 잠김"으로 안내
                long remainingSeconds = Duration.between(LocalDateTime.now(), user.getLockedUntil()).getSeconds();
                log.warn("비밀번호 5회 연속 오류로 계정 잠금 처리됨 [아이디: {}]", user.getUsername());
                throw new AccountLockedException(UserErrorCode.ACCOUNT_LOCKED, remainingSeconds);
            }

            log.warn("비밀번호 입력 오류 [아이디: {}, 누적 실패: {}/{}]",
                    user.getUsername(), user.getLoginFailCount(), User.MAX_LOGIN_FAIL_COUNT);
            throw new InvalidPasswordException(UserErrorCode.INVALID_PASSWORD, user.getLoginFailCount(), User.MAX_LOGIN_FAIL_COUNT);
        }

        // [3] 성공 처리 (분리된 수정 트랜잭션 호출)
        resetLoginFailure(user);

        String accessToken = globalJwtProvider.createUserAccessToken(user.getEmail());
        String refreshToken = globalJwtProvider.createUserRefreshToken(user.getEmail());

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                RedisKeys.REFRESH_TOKEN_PREFIX + user.getEmail(),
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        // 이중 로그인 방지: 이 로그인이 "현재 활성 세션"이 되도록 표시 (기존에 다른 기기에서 로그인해 있었다면 그 세션은 다음 요청부터 즉시 튕겨나감)
        redisTemplate.opsForValue().set(
                RedisKeys.ACTIVE_AT_PREFIX + user.getEmail(),
                accessToken,
                accessTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        log.info("로그인 로직 통과 및 토큰 생성 완료 [아이디: {}, AccessToken: {}...]",
                user.getUsername(), accessToken.substring(0, 15));

        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                user.getRequiresPasswordChange(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    // ---------------------------------------------------------
    // 트랜잭션이 필요한 조각들 (이 메서드들은 반드시 protected 여야 프록시가 작동합니다!)
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    protected User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
    }

    @Transactional
    protected void updateLoginFailure(User user) {
        user.increaseLoginFailure();
        userRepository.save(user);
    }

    @Transactional
    protected void resetLoginFailure(User user) {
        user.resetLoginFailure();
        userRepository.save(user);
    }

    // 5. 아이디 찾기
    @Transactional(readOnly = true)
    public FindIdResponse findId(FindIdRequest request) {
        User user = userRepository.findByNameAndEmail(request.name(), request.email().toLowerCase())
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String username = user.getUsername();
        String maskedId = maskId(username);

        log.info("아이디 찾기 완료 [요청 이메일: {}, 마스킹된 아이디 반환: {}]", request.email(), maskedId);

        return new FindIdResponse(maskedId);
    }

    // 6. 비밀번호 찾기
    @Transactional
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
    @Transactional
    public void resetPassword(String email, ResetPasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (!user.getRequiresPasswordChange()) {
            log.warn("비정상적인 비밀번호 강제 변경 시도 탐지 (변경 대상이 아님) [이메일: {}]", email);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호가 임시비번 발급 전(잊어버렸던) 원래 비밀번호와 같으면 변경 거부
        if (user.getPreviousPassword() != null
                && passwordEncoder.matches(request.newPassword(), user.getPreviousPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_SAME_AS_OLD);
        }

        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        user.changePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 강제 변경 완료 [이메일: {}]", email);
    }

    // 8. 로그아웃
    public void logout(String email) {
        // Redis에서 해당 유저의 Refresh Token 삭제
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + email);

        // 🌟 그냥 삭제하면 "한 번도 로그인 기록 없음"과 구분이 안 돼서 필터가 fail-open으로 통과시켜버림
        //    -> 절대 어떤 토큰 문자열과도 일치할 수 없는 값으로 덮어써서, 이 계정의 토큰은 로그아웃 즉시(만료 전이라도) 전부 무효화되도록 함
        redisTemplate.opsForValue().set(
                RedisKeys.ACTIVE_AT_PREFIX + email,
                "LOGGED_OUT",
                accessTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        log.info("로그아웃 처리 완료 [접속 종료 이메일: {}]", email);
    }

    // 토큰 재발급을 위한 검증 메서드
    public String refreshAccessToken(String email, String refreshToken) {
        // 블랙리스트 여부 확인하여 재발급 차단
        String isBlacklisted = redisTemplate.opsForValue().get(RedisKeys.BLACKLIST_PREFIX + email);
        if ("true".equals(isBlacklisted)) {
            log.warn("블랙리스트 유저의 토큰 재발급 시도 차단 [이메일: {}]", email);
            throw new AuthException(AuthErrorCode.BLACKLISTED_USER);
        }

        String savedRefreshToken = redisTemplate.opsForValue().get(RedisKeys.REFRESH_TOKEN_PREFIX + email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 🌟 idle 타임아웃 체크: ACTIVE_AT은 요청이 있을 때마다 필터에서 TTL이 갱신되므로,
        //    이게 비어있다는 건 accessTokenExpiration(30분)보다 오래 아무 활동도 없었다는 뜻 -> 세션 강제 종료
        String activeAccessToken = redisTemplate.opsForValue().get(RedisKeys.ACTIVE_AT_PREFIX + email);
        if (activeAccessToken == null) {
            redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + email);
            throw new AuthException(AuthErrorCode.SESSION_IDLE_TIMEOUT);
        }

        String newAccessToken = globalJwtProvider.createUserAccessToken(email);

        // 재발급도 "같은 세션의 연장"이므로 활성 세션 표시를 새 토큰으로 갱신 (여기서 안 갱신하면 재발급 직후 본인 요청이 바로 튕겨나감)
        redisTemplate.opsForValue().set(
                RedisKeys.ACTIVE_AT_PREFIX + email,
                newAccessToken,
                accessTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        return newAccessToken;
    }

    // 소셜 전용 추가정보 회원가입
    @Transactional
    public void socialSignup(AuthSocialSignupRequest request) {
        String email = request.email().toLowerCase();

        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 1-0. 탈퇴 후 30일 이내에 같은 이메일로 재가입 시도하는지 확인
        if (redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + email)) {
            throw new AuthException(AuthErrorCode.RECENTLY_WITHDRAWN_EMAIL);
        }

        // 1-1. 전화번호 중복 검사 (일반 회원가입/마이페이지 수정과 동일하게 체크)
        if (userRepository.existsByPhone(request.phone())) {
            throw new UserException(UserErrorCode.ALREADY_EXISTS_PHONE);
        }

        // 2. 요구사항 명세 반영: 아이디(username)는 이메일로 대체
        String username = email;

        // 3. 요구사항 명세 반영: 소셜 유저용 더미(랜덤) 비밀번호 생성
        String dummyPassword = UUID.randomUUID().toString();

        Long referrerUserId = resolveReferrerUserId(request.referralCode());

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
//                .personalCode(UUID.randomUUID().toString())
                .personalCode(generateUniquePersonalCode()) // 6자로 난수 생성 메서드
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
        eventPublisher.publishEvent(new UserSignedUpEvent(
                savedUser.getId(),
                referrerUserId,
                normalizeReferralCode(request.referralCode())
        ));
        log.info("[Signup] UserSignedUpEvent published. userId={}, referrerUserId={}", savedUser.getId(), referrerUserId);

        log.info("소셜 신규 회원가입 완료 [아이디(이메일): {}, 소셜: {}]",
                user.getUsername(), user.getSocialType());
    }

    // 소셜로그인 리다이렉트 및 토큰 발급 (최신 쿠키 전용 버전)
    @Override
    public SocialAuthResult processLoginAndGetRedirectUrl(String email, String name, String socialType) {
        // 이미 가입된 유저인지 DB 확인
        boolean isExistingUser = userRepository.findByEmailAndIsDeletedFalse(email).isPresent();

        if (isExistingUser) {
            // 블랙리스트 여부 확인
            String isBlacklisted = redisTemplate.opsForValue().get(RedisKeys.BLACKLIST_PREFIX + email);
            if ("true".equals(isBlacklisted)) {
                log.warn("블랙리스트 유저의 소셜 로그인 시도 차단 [이메일: {}]", email);
                // 블랙리스트 차단 시 프론트엔드의 에러 페이지나 처리 화면으로 리다이렉트 (선택 사항)
                return new SocialAuthResult(frontendBaseUrl + "/login?error=blacklisted", null, null);
            }

            String accessToken = globalJwtProvider.createUserAccessToken(email);
            String refreshToken = globalJwtProvider.createUserRefreshToken(email);

            // 기존 일반 로그인과 똑같이 7일간 Redis에 저장
            redisTemplate.opsForValue().set(RedisKeys.REFRESH_TOKEN_PREFIX + email, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
            // 이중 로그인 방지: 일반 로그인과 동일하게 활성 세션 표시 (기존에 다른 기기 세션이 있었다면 즉시 무효화됨)
            redisTemplate.opsForValue().set(RedisKeys.ACTIVE_AT_PREFIX + email, accessToken, accessTokenExpiration, TimeUnit.MILLISECONDS);
            log.info("소셜 로그인 성공 (기존 유저) [이메일: {}]", email);

            // URL 파라미터 전부 제거! (토큰은 쿠키로 구울 거니까)
            String redirectUrl = frontendBaseUrl + "/auth/oauth-callback";
            return new SocialAuthResult(redirectUrl, accessToken, refreshToken);
        } else {
            log.info("소셜 로그인 (신규 유저 발견, 회원가입 유도) [이메일: {}]", email);

            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);

            String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/auth/register")
                    .queryParam("email", email)
                    .queryParam("name", encodedName)
                    .queryParam("socialType", socialType)
                    .build().toUriString();
            return new SocialAuthResult(redirectUrl, null, null);
        }
    }

    private Long resolveReferrerUserId(String referralCode) {
        String normalizedReferralCode = normalizeReferralCode(referralCode);
        if (normalizedReferralCode == null) {
            return null;
        }

        return userRepository.findByPersonalCode(normalizedReferralCode)
                .filter(referrer -> !referrer.isDeleted())
                .map(User::getId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFERRAL_CODE));
    }

    private String normalizeReferralCode(String referralCode) {
        if (referralCode == null || referralCode.isBlank()) {
            return null;
        }

        return referralCode.trim();
    }

    // 중복 없는 6자리 고유 코드를 생성하는 내부 메서드
    private String generateUniquePersonalCode() {
        String personalCode;
        do {
            personalCode = com.kidmily.algoga_server.user.util.PersonalCodeGenerator.generate();
        } while (userRepository.existsByPersonalCode(personalCode));
        return personalCode;
    }
}