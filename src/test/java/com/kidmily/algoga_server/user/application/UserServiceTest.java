package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.global.util.RedisKeys;
import com.kidmily.algoga_server.refund.application.usecase.RefundQueryUseCase;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.settings.UserStorageSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*
 * UserService 단위 테스트
 * - withdraw()가 새로 추가된 탈퇴 전용 이메일 인증 마커를 확인하는지
 * - updateProfile()/updatePassword()가 서로 다른 액션 전용 마커를 쓰는지 (마커 공유로 인한 상호 간섭 버그 재발 방지)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "user@example.com";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GlobalJwtProvider globalJwtProvider;
    @Mock private FileStoragePort fileStoragePort;
    @Mock private UserStorageSettings storageSettings;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private BookingQueryUseCase bookingQueryUseCase;
    @Mock private RefundQueryUseCase refundQueryUseCase;
    @Mock private EmailVerificationHelper emailVerificationHelper;

    @InjectMocks
    private UserService userService;

    private User activeUser() {
        return User.builder()
                .id(1L)
                .username("user01")
                .email(EMAIL)
                .password("encoded-password")
                .name("Test User")
                .phone("01012345678")
                .nickname("nick")
                .socialType(SocialType.LOCAL)
                .personalCode("ABC123")
                // requiresPasswordChange는 @Builder.Default로 false가 기본 적용된다.
                // (혹시 그 어노테이션이 빠지면 null -> AuthTokenResponse의 primitive boolean 언박싱 시 NPE로 이 테스트가 잡아준다)
                .build();
    }

    @Test
    @DisplayName("탈퇴 전용 마커가 없으면 예외가 발생하고, 예약/환불 체크나 탈퇴 이벤트 발행까지 도달하지 않는다")
    void withdraw_notVerified_throwsAndStopsEarly() {
        User user = activeUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        doThrow(new AuthException(AuthErrorCode.EMAIL_NOT_VERIFIED))
                .when(emailVerificationHelper).assertVerified(EMAIL, RedisKeys.MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX);

        assertThrows(AuthException.class, () -> userService.withdraw(EMAIL));

        verify(bookingQueryUseCase, never()).hasActiveBooking(anyLong());
        verify(refundQueryUseCase, never()).hasActiveRefund(anyLong());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("탈퇴 전용 마커가 있으면 정상 탈퇴 처리되고, 탈퇴 마커/토큰 삭제 + 30일 재가입 쿨다운 마커 설정 + 이벤트가 발행된다")
    void withdraw_verified_succeedsAndConsumesOwnMarker() {
        User user = activeUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(bookingQueryUseCase.hasActiveBooking(1L)).thenReturn(false);
        when(refundQueryUseCase.hasActiveRefund(1L)).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        userService.withdraw(EMAIL);

        verify(emailVerificationHelper).assertVerified(EMAIL, RedisKeys.MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX);
        assertThat(user.isDeleted()).isTrue();
        verify(redisTemplate).delete(RedisKeys.REFRESH_TOKEN_PREFIX + EMAIL);
        verify(redisTemplate).delete(RedisKeys.MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX + EMAIL);
        verify(valueOperations).set(RedisKeys.WITHDRAWN_EMAIL_PREFIX + EMAIL, "true", 30L, TimeUnit.DAYS);
        verify(eventPublisher).publishEvent(any(UserWithdrawnEvent.class));
    }

    @Test
    @DisplayName("진행 중인 예약이 있으면 인증을 통과해도 탈퇴가 거부된다")
    void withdraw_activeBooking_throws() {
        User user = activeUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(bookingQueryUseCase.hasActiveBooking(1L)).thenReturn(true);

        assertThrows(UserException.class, () -> userService.withdraw(EMAIL));

        verify(refundQueryUseCase, never()).hasActiveRefund(anyLong());
    }

    @Test
    @DisplayName("updateProfile은 프로필 전용 마커만 확인/삭제하고 비밀번호 마커는 건드리지 않는다")
    void updateProfile_usesProfileScopedMarkerOnly() {
        User user = activeUser();
        when(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(Optional.of(user));
        when(globalJwtProvider.createUserAccessToken(EMAIL)).thenReturn("new-access-token");

        UpdateProfileRequest request = new UpdateProfileRequest("newNick", null, null);

        userService.updateProfile(EMAIL, request);

        verify(emailVerificationHelper).assertVerified(EMAIL, RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX);
        verify(emailVerificationHelper, never()).assertVerified(EMAIL, RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX);
        verify(redisTemplate).delete(RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX + EMAIL);
        verify(redisTemplate, never()).delete(RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX + EMAIL);
    }

    @Test
    @DisplayName("updatePassword는 비밀번호 전용 마커만 확인/삭제하고 프로필 마커는 건드리지 않는다 (핵심 버그 재발 방지 케이스)")
    void updatePassword_usesPasswordScopedMarkerOnly() {
        User user = activeUser();
        when(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPw", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPw123", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPw123")).thenReturn("encoded-new-pw");

        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPw", "newPw123");

        userService.updatePassword(EMAIL, request);

        verify(emailVerificationHelper).assertVerified(EMAIL, RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX);
        verify(emailVerificationHelper, never()).assertVerified(EMAIL, RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX);
        verify(redisTemplate).delete(RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX + EMAIL);
        verify(redisTemplate, never()).delete(RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX + EMAIL);
    }

    @Test
    @DisplayName("비밀번호 변경이 중간에 실패해도 Redis 마커는 전혀 삭제되지 않는다 (재시도 시 재인증 없이 가능해야 함)")
    void updatePassword_failure_doesNotConsumeAnyMarker() {
        User user = activeUser();
        when(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPw", user.getPassword())).thenReturn(false);

        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPw", "newPw123");

        assertThrows(UserException.class, () -> userService.updatePassword(EMAIL, request));

        verify(redisTemplate, never()).delete(anyString());
    }
}
