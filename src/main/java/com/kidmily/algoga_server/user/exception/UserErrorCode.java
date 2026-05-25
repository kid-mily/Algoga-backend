package com.kidmily.algoga_server.user.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    ALREADY_EXISTS_EMAIL(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 이메일입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER_002", "존재하지 않는 계정입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_003", "아이디 또는 비밀번호가 틀렸습니다."),
    DELETED_USER(HttpStatus.FORBIDDEN, "USER_004", "탈퇴한 계정입니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "USER_005", "비밀번호 5회 오류로 인해 5분간 계정이 잠겼습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}