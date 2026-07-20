package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.infrastructure.mail.EmailSender;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * EmailVerificationHelper 단위 테스트
 * - AuthService/UserService가 복붙해서 갖고 있던 인증 로직을 공용화한 헬퍼 검증
 * - verifyCode가 여러 성공 마커를 동시에 세팅하는지 (마이페이지 인증 1회 -> 프로필/비밀번호/탈퇴 마커 분리)
 * - assertVerified가 액션별 마커를 독립적으로 확인하는지
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationHelperTest {

    private static final String EMAIL = "user@example.com";
    private static final String CODE_PREFIX = "TEST_CODE:";
    private static final String SUCCESS_PREFIX_A = "TEST_SUCCESS_A:";
    private static final String SUCCESS_PREFIX_B = "TEST_SUCCESS_B:";

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private EmailSender emailSender;

    @InjectMocks
    private EmailVerificationHelper emailVerificationHelper;

    @Test
    @DisplayName("인증코드 발송 시 Redis에 3분 TTL로 저장되고 이메일이 발송된다")
    void sendCode_storesCodeAndSendsEmail() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        emailVerificationHelper.sendCode(EMAIL, CODE_PREFIX, "제목", "안내문구");

        verify(valueOperations).set(eq(CODE_PREFIX + EMAIL), anyString(), eq(3L), eq(TimeUnit.MINUTES));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendEmail(eq(EMAIL), eq("제목"), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("안내문구").contains("인증번호");
    }

    @Test
    @DisplayName("코드 검증 성공 시 코드 키는 삭제되고 전달한 마커가 전부 세팅된다")
    void verifyCode_success_setsAllSuccessMarkers() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CODE_PREFIX + EMAIL)).thenReturn("123456");

        emailVerificationHelper.verifyCode(EMAIL, "123456", CODE_PREFIX, SUCCESS_PREFIX_A, SUCCESS_PREFIX_B);

        verify(redisTemplate).delete(CODE_PREFIX + EMAIL);
        verify(valueOperations).set(eq(SUCCESS_PREFIX_A + EMAIL), eq("true"), eq(30L), eq(TimeUnit.MINUTES));
        verify(valueOperations).set(eq(SUCCESS_PREFIX_B + EMAIL), eq("true"), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("저장된 코드가 없으면 인증 실패 예외가 발생한다")
    void verifyCode_noSavedCode_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CODE_PREFIX + EMAIL)).thenReturn(null);

        AuthException ex = assertThrows(AuthException.class,
                () -> emailVerificationHelper.verifyCode(EMAIL, "123456", CODE_PREFIX, SUCCESS_PREFIX_A));
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_AUTH_CODE_MISMATCH);
    }

    @Test
    @DisplayName("입력한 코드가 저장된 코드와 다르면 인증 실패 예외가 발생한다")
    void verifyCode_mismatch_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CODE_PREFIX + EMAIL)).thenReturn("123456");

        assertThrows(AuthException.class,
                () -> emailVerificationHelper.verifyCode(EMAIL, "000000", CODE_PREFIX, SUCCESS_PREFIX_A));
    }

    @Test
    @DisplayName("성공 마커가 true면 예외 없이 통과한다")
    void assertVerified_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(SUCCESS_PREFIX_A + EMAIL)).thenReturn("true");

        emailVerificationHelper.assertVerified(EMAIL, SUCCESS_PREFIX_A);
    }

    @Test
    @DisplayName("성공 마커가 없으면 이메일 미인증 예외가 발생한다")
    void assertVerified_missingMarker_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(SUCCESS_PREFIX_A + EMAIL)).thenReturn(null);

        AuthException ex = assertThrows(AuthException.class,
                () -> emailVerificationHelper.assertVerified(EMAIL, SUCCESS_PREFIX_A));
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("다른 액션의 마커가 세팅돼 있어도 내 액션 마커가 없으면 예외가 발생한다 (마커 독립성 검증)")
    void assertVerified_differentMarker_notShared() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // SUCCESS_PREFIX_A 마커만 존재하고 SUCCESS_PREFIX_B 마커는 없는 상황
        when(valueOperations.get(SUCCESS_PREFIX_B + EMAIL)).thenReturn(null);

        assertThrows(AuthException.class,
                () -> emailVerificationHelper.assertVerified(EMAIL, SUCCESS_PREFIX_B));
    }
}
