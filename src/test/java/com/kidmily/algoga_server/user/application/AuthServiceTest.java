package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.event.UserSignedUpEvent;
import com.kidmily.algoga_server.global.exception.AccountLockedException;
import com.kidmily.algoga_server.global.exception.InvalidPasswordException;
import com.kidmily.algoga_server.global.infrastructure.mail.EmailSender;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.global.security.dto.SocialAuthResult;
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
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/*
 * AuthService 단위 테스트 (가입/로그인/토큰재발급 등 핵심 인증 로직에 대한 첫 유닛테스트).
 * - @Value로 주입되는 frontendBaseUrl/accessTokenExpiration/refreshTokenExpiration은
 *   Mockito @InjectMocks가 채워주지 않으므로 ReflectionTestUtils로 직접 세팅한다.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final long ACCESS_TOKEN_EXPIRATION = 1_800_000L; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 1_209_600_000L; // 14일
    private static final String FRONTEND_BASE_URL = "https://algoga.com";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GlobalJwtProvider globalJwtProvider;
    @Mock private EmailSender emailSender;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private EmailVerificationHelper emailVerificationHelper;
    @Mock private Counter userSignupLocalTotal;
    @Mock private Counter userSignupGoogleTotal;
    @Mock private Counter userSignupKakaoTotal;
    @Mock private Counter userLoginSuccessTotal;
    @Mock private Counter userLoginFailedTotal;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendBaseUrl", FRONTEND_BASE_URL);
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
        // Counter 타입 목(mock)이 여러 개라 @InjectMocks의 생성자 주입이 필드명으로 정확히 매칭을 못 해줘서,
        // 각각을 명시적으로 다시 꽂아준다 (안 그러면 엉뚱한 Counter가 꽂혀 verify()가 실패함).
        ReflectionTestUtils.setField(authService, "userSignupLocalTotal", userSignupLocalTotal);
        ReflectionTestUtils.setField(authService, "userSignupGoogleTotal", userSignupGoogleTotal);
        ReflectionTestUtils.setField(authService, "userSignupKakaoTotal", userSignupKakaoTotal);
        ReflectionTestUtils.setField(authService, "userLoginSuccessTotal", userLoginSuccessTotal);
        ReflectionTestUtils.setField(authService, "userLoginFailedTotal", userLoginFailedTotal);
    }

    private User.UserBuilder baseUserBuilder() {
        return User.builder()
                .id(1L)
                .username("user01")
                .email(EMAIL)
                .password("encoded-password")
                .name("Test User")
                .phone("01012345678")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .nickname("nick")
                .socialType(SocialType.LOCAL)
                .personalCode("ABC123")
                .termsServiceAgreed(true)
                .termsPrivacyAgreed(true)
                .termsMarketingAgreed(false);
    }

    private User activeUser() {
        return baseUserBuilder().build();
    }

    private AuthSignupRequest signupRequest(String referralCode) {
        return new AuthSignupRequest(
                "newUser", EMAIL, "password123", "New User", "010-1111-2222",
                LocalDate.of(2000, 1, 1), "MALE", "newbie", referralCode, "instagram",
                true, true, false
        );
    }

    // ===================== sendVerificationCode =====================

    @Test
    @DisplayName("이미 가입된 이메일이면 인증번호 발송 전에 즉시 거부된다")
    void sendVerificationCode_duplicateEmail_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.sendVerificationCode(new SendEmailCodeRequest(EMAIL)));

        verifyNoInteractions(emailVerificationHelper);
    }

    @Test
    @DisplayName("30일 재가입 쿨다운 대상 이메일이면 인증번호 발송이 거부된다")
    void sendVerificationCode_recentlyWithdrawnEmail_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.sendVerificationCode(new SendEmailCodeRequest(EMAIL)));

        verifyNoInteractions(emailVerificationHelper);
    }

    @Test
    @DisplayName("중복/쿨다운 대상이 아니면 인증번호 발송 헬퍼에 위임한다")
    void sendVerificationCode_success_delegatesToHelper() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);

        authService.sendVerificationCode(new SendEmailCodeRequest(EMAIL));

        verify(emailVerificationHelper).sendCode(eq(EMAIL), eq(RedisKeys.AUTH_CODE_PREFIX), anyString(), anyString());
    }

    // ===================== verifyEmailCode =====================

    @Test
    @DisplayName("이메일 인증번호 확인은 그대로 헬퍼에 위임한다")
    void verifyEmailCode_delegatesToHelper() {
        authService.verifyEmailCode(new VerifyEmailCodeRequest(EMAIL, "123456"));

        verify(emailVerificationHelper).verifyCode(EMAIL, "123456", RedisKeys.AUTH_CODE_PREFIX, RedisKeys.AUTH_SUCCESS_PREFIX);
    }

    // ===================== signup =====================

    @Test
    @DisplayName("이미 가입된 이메일이면 회원가입이 거부된다")
    void signup_duplicateEmail_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.signup(signupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("30일 재가입 쿨다운 대상 이메일이면 회원가입이 거부된다")
    void signup_recentlyWithdrawnEmail_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.signup(signupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("아이디가 중복이면 회원가입이 거부된다")
    void signup_duplicateUsername_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.signup(signupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("전화번호가 중복이면 회원가입이 거부된다")
    void signup_duplicatePhone_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(true);

        assertThrows(UserException.class, () -> authService.signup(signupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일 인증을 완료하지 않았으면 회원가입이 거부된다")
    void signup_emailNotVerified_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.AUTH_SUCCESS_PREFIX + EMAIL)).thenReturn(null);

        assertThrows(AuthException.class, () -> authService.signup(signupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("추천인 코드가 존재하지 않는 개인코드면 회원가입이 거부된다")
    void signup_invalidReferralCode_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.AUTH_SUCCESS_PREFIX + EMAIL)).thenReturn("true");
        when(userRepository.findByPersonalCode("REF999")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.signup(signupRequest("REF999")));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("모든 검증을 통과하면 회원이 저장되고, 가입 이벤트 발행 + 인증 마커 삭제가 이뤄진다")
    void signup_success_savesUserAndPublishesEventAndClearsMarker() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.AUTH_SUCCESS_PREFIX + EMAIL)).thenReturn("true");
        when(userRepository.existsByPersonalCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 10L);
            return u;
        });

        authService.signup(signupRequest(null));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password123");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);

        ArgumentCaptor<UserSignedUpEvent> eventCaptor = ArgumentCaptor.forClass(UserSignedUpEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(10L);
        assertThat(eventCaptor.getValue().referrerUserId()).isNull();

        verify(redisTemplate).delete(RedisKeys.AUTH_SUCCESS_PREFIX + EMAIL);
        verify(userSignupLocalTotal).increment();
    }

    @Test
    @DisplayName("유효한 추천인 코드가 있으면 추천인의 userId가 이벤트에 실려 발행된다")
    void signup_withValidReferralCode_resolvesReferrerUserId() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.AUTH_SUCCESS_PREFIX + EMAIL)).thenReturn("true");
        User referrer = baseUserBuilder().id(99L).build();
        when(userRepository.findByPersonalCode("REF123")).thenReturn(Optional.of(referrer));
        when(userRepository.existsByPersonalCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.signup(signupRequest("REF123"));

        ArgumentCaptor<UserSignedUpEvent> eventCaptor = ArgumentCaptor.forClass(UserSignedUpEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().referrerUserId()).isEqualTo(99L);
        assertThat(eventCaptor.getValue().referralCode()).isEqualTo("REF123");
    }

    // ===================== isUsernameAvailable / isPhoneAvailable =====================

    @Test
    @DisplayName("이미 존재하는 아이디면 사용 불가로 판단한다")
    void isUsernameAvailable_existing_returnsFalse() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThat(authService.isUsernameAvailable("taken")).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 전화번호면 사용 가능으로 판단한다")
    void isPhoneAvailable_notExisting_returnsTrue() {
        when(userRepository.existsByPhone("01099998888")).thenReturn(false);

        assertThat(authService.isPhoneAvailable("01099998888")).isTrue();
    }

    // ===================== login =====================

    @Test
    @DisplayName("탈퇴한 계정으로는 로그인할 수 없다")
    void login_deletedUser_throws() {
        User user = baseUserBuilder().isDeleted(true).build();
        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));

        assertThrows(UserException.class, () -> authService.login(new AuthLoginRequest("user01", "pw")));

        verifyNoInteractions(passwordEncoder);
        verify(userLoginFailedTotal).increment();
        verifyNoInteractions(userLoginSuccessTotal);
    }

    @Test
    @DisplayName("잠긴 계정이면 비밀번호 검증 없이 즉시 남은 잠금 시간과 함께 거부된다")
    void login_accountLocked_throws() {
        User user = baseUserBuilder().lockedUntil(LocalDateTime.now().plusMinutes(5)).build();
        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class, () -> authService.login(new AuthLoginRequest("user01", "pw")));

        verifyNoInteractions(passwordEncoder);
        verify(userLoginFailedTotal).increment();
    }

    @Test
    @DisplayName("블랙리스트 계정이면 비밀번호가 맞아도 로그인이 거부된다")
    void login_blacklisted_throws() {
        User user = activeUser();
        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn("true");

        assertThrows(AuthException.class, () -> authService.login(new AuthLoginRequest("user01", "pw")));

        verifyNoInteractions(passwordEncoder);
        verify(userLoginFailedTotal).increment();
    }

    @Test
    @DisplayName("비밀번호가 틀리면 실패 횟수가 증가하고 남은 시도 횟수와 함께 예외가 발생한다")
    void login_wrongPassword_incrementsFailureAndThrowsInvalidPassword() {
        User user = activeUser();
        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(passwordEncoder.matches("wrongPw", "encoded-password")).thenReturn(false);

        InvalidPasswordException ex = assertThrows(InvalidPasswordException.class,
                () -> authService.login(new AuthLoginRequest("user01", "wrongPw")));

        assertThat(ex.getFailCount()).isEqualTo(1);
        assertThat(ex.getMaxAttempts()).isEqualTo(User.MAX_LOGIN_FAIL_COUNT);
        assertThat(user.getLoginFailCount()).isEqualTo(1);
        verify(userRepository).save(user);
        verify(userLoginFailedTotal).increment();
    }

    @Test
    @DisplayName("실패 횟수가 최대치에 도달하면 그 순간 계정이 잠기고 잠김 예외가 발생한다")
    void login_wrongPasswordReachesMax_locksAccountAndThrowsAccountLocked() {
        User user = baseUserBuilder().loginFailCount(User.MAX_LOGIN_FAIL_COUNT - 1).build();
        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(passwordEncoder.matches("wrongPw", "encoded-password")).thenReturn(false);

        assertThrows(AccountLockedException.class, () -> authService.login(new AuthLoginRequest("user01", "wrongPw")));

        assertThat(user.isAccountLocked()).isTrue();
        verify(userLoginFailedTotal).increment();
    }

    @Test
    @DisplayName("로그인에 성공하면 실패 횟수가 리셋되고 토큰이 발급되어 Redis에 저장된다")
    void login_success_issuesTokensAndSetsRedisAndResetsFailure() {
        User user = baseUserBuilder().loginFailCount(3).build();
        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(passwordEncoder.matches("correctPw", "encoded-password")).thenReturn(true);
        when(globalJwtProvider.createUserAccessToken(EMAIL)).thenReturn("access-token-1234567890");
        when(globalJwtProvider.createUserRefreshToken(EMAIL)).thenReturn("refresh-token-1234567890");

        AuthTokenResponse response = authService.login(new AuthLoginRequest("user01", "correctPw"));

        assertThat(response.accessToken()).isEqualTo("access-token-1234567890");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-1234567890");
        assertThat(response.nickname()).isEqualTo("nick");
        assertThat(user.getLoginFailCount()).isZero();
        verify(valueOperations).set(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL, "refresh-token-1234567890", REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        verify(valueOperations).set(RedisKeys.ACTIVE_AT_PREFIX + EMAIL, "access-token-1234567890", ACCESS_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        verify(userLoginSuccessTotal).increment();
        verifyNoInteractions(userLoginFailedTotal);
    }

    // ===================== refreshAccessToken =====================

    @Test
    @DisplayName("블랙리스트 계정은 토큰 재발급이 거부된다")
    void refreshAccessToken_blacklisted_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn("true");

        assertThrows(AuthException.class, () -> authService.refreshAccessToken(EMAIL, "some-refresh-token"));
    }

    @Test
    @DisplayName("저장된 refresh token과 일치하지 않으면 재발급이 거부된다")
    void refreshAccessToken_refreshTokenMismatch_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(valueOperations.get(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL)).thenReturn("different-token");

        assertThrows(AuthException.class, () -> authService.refreshAccessToken(EMAIL, "some-refresh-token"));
    }

    @Test
    @DisplayName("ACTIVE_AT가 없으면(유휴 타임아웃) refresh token을 삭제하고 세션 만료 예외를 던진다")
    void refreshAccessToken_idleTimeout_deletesRefreshTokenAndThrows() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(valueOperations.get(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL)).thenReturn("saved-refresh-token");
        when(valueOperations.get(RedisKeys.ACTIVE_AT_PREFIX + EMAIL)).thenReturn(null);

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.refreshAccessToken(EMAIL, "saved-refresh-token"));

        verify(redisTemplate).delete(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL);
        verify(globalJwtProvider, never()).createUserAccessToken(anyString());
    }

    @Test
    @DisplayName("정상 상태면 새 access token을 발급하고 ACTIVE_AT을 갱신한다")
    void refreshAccessToken_success_issuesNewTokenAndUpdatesActiveAt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(valueOperations.get(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL)).thenReturn("saved-refresh-token");
        when(valueOperations.get(RedisKeys.ACTIVE_AT_PREFIX + EMAIL)).thenReturn("old-access-token");
        when(globalJwtProvider.createUserAccessToken(EMAIL)).thenReturn("new-access-token");

        String result = authService.refreshAccessToken(EMAIL, "saved-refresh-token");

        assertThat(result).isEqualTo("new-access-token");
        verify(valueOperations).set(RedisKeys.ACTIVE_AT_PREFIX + EMAIL, "new-access-token", ACCESS_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        verify(redisTemplate, never()).delete(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL);
    }

    // ===================== logout =====================

    @Test
    @DisplayName("로그아웃하면 refresh token이 삭제되고 ACTIVE_AT은 로그아웃 상태값으로 대체된다")
    void logout_deletesRefreshTokenAndSetsLoggedOutSentinel() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout(EMAIL);

        verify(redisTemplate).delete(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL);
        verify(valueOperations).set(RedisKeys.ACTIVE_AT_PREFIX + EMAIL, "LOGGED_OUT", ACCESS_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    // ===================== resetPassword =====================

    @Test
    @DisplayName("비밀번호 강제 변경 대상이 아니면 거부된다")
    void resetPassword_notRequired_throws() {
        User user = baseUserBuilder().requiresPasswordChange(false).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(UserException.class, () -> authService.resetPassword(EMAIL, new ResetPasswordRequest("newPassword123")));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("새 비밀번호가 이전 비밀번호와 같으면 거부된다")
    void resetPassword_sameAsPreviousPassword_throws() {
        User user = baseUserBuilder().requiresPasswordChange(true).previousPassword("old-encoded-pw").build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("newPassword123", "old-encoded-pw")).thenReturn(true);

        assertThrows(UserException.class, () -> authService.resetPassword(EMAIL, new ResetPasswordRequest("newPassword123")));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상적으로 비밀번호가 변경되면 강제 변경 플래그가 해제되고 이전 비밀번호 스냅샷이 비워진다")
    void resetPassword_success_changesPassword() {
        User user = baseUserBuilder().requiresPasswordChange(true).previousPassword("old-encoded-pw").build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("newPassword123", "old-encoded-pw")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-pw");

        authService.resetPassword(EMAIL, new ResetPasswordRequest("newPassword123"));

        assertThat(user.getPassword()).isEqualTo("new-encoded-pw");
        assertThat(user.getRequiresPasswordChange()).isFalse();
        assertThat(user.getPreviousPassword()).isNull();
        verify(userRepository).save(user);
    }

    // ===================== socialSignup =====================

    @Test
    @DisplayName("소셜 추가정보 가입도 이메일 중복이면 거부된다")
    void socialSignup_duplicateEmail_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.socialSignup(socialSignupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("소셜 추가정보 가입도 30일 재가입 쿨다운 대상이면 거부된다")
    void socialSignup_recentlyWithdrawnEmail_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.socialSignup(socialSignupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("소셜 추가정보 가입도 전화번호가 중복이면 거부된다")
    void socialSignup_duplicatePhone_throws() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(true);

        assertThrows(UserException.class, () -> authService.socialSignup(socialSignupRequest(null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("소셜 추가정보 가입이 성공하면 이메일을 아이디로 사용해 저장되고 가입 이벤트가 발행된다")
    void socialSignup_success_savesUserAndPublishesEvent() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(redisTemplate.hasKey(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL)).thenReturn(false);
        when(userRepository.existsByPhone("010-1111-2222")).thenReturn(false);
        when(userRepository.existsByPersonalCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-dummy-pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 20L);
            return u;
        });

        authService.socialSignup(socialSignupRequest(null));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo(EMAIL);
        assertThat(userCaptor.getValue().getSocialType()).isEqualTo(SocialType.GOOGLE);

        ArgumentCaptor<UserSignedUpEvent> eventCaptor = ArgumentCaptor.forClass(UserSignedUpEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(20L);

        verify(userSignupGoogleTotal).increment();
        verifyNoInteractions(userSignupKakaoTotal, userSignupLocalTotal);
    }

    private AuthSocialSignupRequest socialSignupRequest(String referralCode) {
        return new AuthSocialSignupRequest(
                EMAIL, "New User", "010-1111-2222", LocalDate.of(2000, 1, 1),
                "MALE", "newbie", "GOOGLE", referralCode, "instagram",
                true, true, false
        );
    }

    // ===================== processLoginAndGetRedirectUrl =====================

    @Test
    @DisplayName("기존 회원이 블랙리스트 상태면 로그인 에러 페이지로 리다이렉트되고 토큰은 발급되지 않는다")
    void processLoginAndGetRedirectUrl_existingUserBlacklisted_redirectsToErrorPage() {
        User user = activeUser();
        when(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn("true");

        SocialAuthResult result = authService.processLoginAndGetRedirectUrl(EMAIL, "Test User", "GOOGLE");

        assertThat(result.redirectUrl()).isEqualTo(FRONTEND_BASE_URL + "/login?error=blacklisted");
        assertThat(result.accessToken()).isNull();
        verify(globalJwtProvider, never()).createUserAccessToken(anyString());
    }

    @Test
    @DisplayName("기존 회원이면 토큰을 발급해 Redis에 저장하고 콜백 페이지로 리다이렉트한다")
    void processLoginAndGetRedirectUrl_existingUser_issuesTokensAndRedirectsToCallback() {
        User user = activeUser();
        when(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeys.BLACKLIST_PREFIX + EMAIL)).thenReturn(null);
        when(globalJwtProvider.createUserAccessToken(EMAIL)).thenReturn("access-token");
        when(globalJwtProvider.createUserRefreshToken(EMAIL)).thenReturn("refresh-token");

        SocialAuthResult result = authService.processLoginAndGetRedirectUrl(EMAIL, "Test User", "GOOGLE");

        assertThat(result.redirectUrl()).isEqualTo(FRONTEND_BASE_URL + "/auth/oauth-callback");
        assertThat(result.accessToken()).isEqualTo("access-token");
        verify(valueOperations).set(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL, "refresh-token", REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("신규 회원이면 토큰 없이 추가정보 입력 페이지로 리다이렉트한다")
    void processLoginAndGetRedirectUrl_newUser_redirectsToRegisterWithParams() {
        when(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(Optional.empty());

        SocialAuthResult result = authService.processLoginAndGetRedirectUrl(EMAIL, "Test User", "GOOGLE");

        assertThat(result.redirectUrl()).startsWith(FRONTEND_BASE_URL + "/auth/register");
        assertThat(result.redirectUrl()).contains("email=" + EMAIL);
        assertThat(result.accessToken()).isNull();
        assertThat(result.refreshToken()).isNull();
        verifyNoInteractions(globalJwtProvider);
    }

    // ===================== findId / findPassword =====================

    @Test
    @DisplayName("아이디 찾기에 성공하면 마스킹된 아이디를 반환한다")
    void findId_success_returnsMaskedId() {
        User user = activeUser();
        when(userRepository.findByNameAndEmail("Test User", EMAIL)).thenReturn(Optional.of(user));

        FindIdResponse response = authService.findId(new FindIdRequest("Test User", EMAIL));

        assertThat(response.maskedId()).isEqualTo("use***");
    }

    @Test
    @DisplayName("일치하는 회원이 없으면 아이디 찾기가 실패한다")
    void findId_notFound_throws() {
        when(userRepository.findByNameAndEmail("Nobody", EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> authService.findId(new FindIdRequest("Nobody", EMAIL)));
    }

    @Test
    @DisplayName("비밀번호 찾기에 성공하면 임시 비밀번호가 발급되어 저장되고 이메일로 발송된다")
    void findPassword_success_setsTemporaryPasswordAndSendsEmail() {
        User user = activeUser();
        when(userRepository.findByUsernameAndEmail("user01", EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-pw");

        authService.findPassword(new FindPasswordRequest("user01", EMAIL));

        assertThat(user.getPassword()).isEqualTo("encoded-temp-pw");
        assertThat(user.getRequiresPasswordChange()).isTrue();
        verify(userRepository).save(user);
        verify(emailSender).sendEmail(eq(EMAIL), anyString(), anyString());
    }
}
