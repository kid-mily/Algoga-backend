package com.kidmily.algoga_server.user.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_002", "비밀번호는 영문, 숫자 조합 8자 이상이어야 합니다."),
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "AUTH_003", "필수 회원 정보가 누락되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}