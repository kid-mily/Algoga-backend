package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.infrastructure.mail.EmailSender;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// AuthService(회원가입 인증)와 UserService(마이페이지 본인확인)가 각자 복붙해서 갖고 있던
// "6자리 코드 생성 -> Redis 저장 -> 이메일 발송"과 "코드 검증 -> 성공 마커로 교체" 로직을 공용화한 헬퍼.
// 두 흐름은 Redis 키 접두사와 이메일 문구만 다르고 나머지 절차는 동일하다.
@Component
@RequiredArgsConstructor
public class EmailVerificationHelper {

    private static final long CODE_TTL_MINUTES = 3;
    private static final long SUCCESS_TTL_MINUTES = 30;

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailSender emailSender;

    public void sendCode(String email, String codeKeyPrefix, String subject, String bodyIntro) {
        String code = String.valueOf((int) (Math.random() * 899999) + 100000);
        redisTemplate.opsForValue().set(codeKeyPrefix + email, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        String body = bodyIntro + "\n"
                + "인증번호 : [" + code + "]\n\n"
                + "3분 이내에 입력해 주세요.";
        emailSender.sendEmail(email, subject, body);
    }

    // 검증 1회로 여러 액션의 성공 마커를 동시에 발급할 수 있도록 successKeyPrefix를 가변인자로 받는다.
    // (마이페이지 인증처럼 "인증 1번 -> 프로필/비밀번호/탈퇴 마커를 각각 독립적으로 소비" 하는 흐름을 지원)
    public void verifyCode(String email, String inputCode, String codeKeyPrefix, String... successKeyPrefixes) {
        String savedCode = redisTemplate.opsForValue().get(codeKeyPrefix + email);

        if (savedCode == null || !savedCode.equals(inputCode)) {
            throw new AuthException(AuthErrorCode.EMAIL_AUTH_CODE_MISMATCH);
        }

        redisTemplate.delete(codeKeyPrefix + email);
        for (String successKeyPrefix : successKeyPrefixes) {
            redisTemplate.opsForValue().set(successKeyPrefix + email, "true", SUCCESS_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }

    // 특정 액션의 인증 마커가 살아있는지 확인만 한다 (소비/삭제는 호출부가 각자 담당).
    public void assertVerified(String email, String successKeyPrefix) {
        String isVerified = redisTemplate.opsForValue().get(successKeyPrefix + email);
        if (!"true".equals(isVerified)) {
            throw new AuthException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }
    }
}
