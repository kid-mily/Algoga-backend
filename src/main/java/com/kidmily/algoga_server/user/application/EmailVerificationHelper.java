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

    public void verifyCode(String email, String inputCode, String codeKeyPrefix, String successKeyPrefix) {
        String savedCode = redisTemplate.opsForValue().get(codeKeyPrefix + email);

        if (savedCode == null || !savedCode.equals(inputCode)) {
            throw new AuthException(AuthErrorCode.EMAIL_AUTH_CODE_MISMATCH);
        }

        redisTemplate.delete(codeKeyPrefix + email);
        redisTemplate.opsForValue().set(successKeyPrefix + email, "true", SUCCESS_TTL_MINUTES, TimeUnit.MINUTES);
    }
}
