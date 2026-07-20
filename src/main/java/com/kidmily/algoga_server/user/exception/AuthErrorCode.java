package com.kidmily.algoga_server.user.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "AUTH_002", "이미 사용 중인 아이디입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_010", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_011", "로그아웃되었거나 만료된 토큰입니다."),

    EMAIL_AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_013", "인증번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_014", "이메일 인증이 완료되지 않았습니다."),
    BLACKLISTED_USER(HttpStatus.FORBIDDEN, "AUTH_015", "블랙리스트에 등록되어 접근이 영구히 제한된 계정입니다. 고객센터에 문의하세요."),
    INVALID_REFERRAL_CODE(HttpStatus.BAD_REQUEST, "AUTH_016", "유효하지 않은 추천인 코드입니다."),
    DUPLICATE_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH_017", "다른 기기에서 로그인되어 세션이 종료되었습니다."),
    SESSION_IDLE_TIMEOUT(HttpStatus.UNAUTHORIZED, "AUTH_018", "장시간 활동이 없어 세션이 만료되었습니다. 다시 로그인해주세요."),
    RECENTLY_WITHDRAWN_EMAIL(HttpStatus.CONFLICT, "AUTH_019", "최근 탈퇴한 이메일입니다. 탈퇴 후 30일이 지나야 재가입할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
