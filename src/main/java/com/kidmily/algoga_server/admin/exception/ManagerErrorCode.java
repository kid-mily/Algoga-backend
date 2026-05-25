package com.kidmily.algoga_server.admin.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ManagerErrorCode implements BaseErrorCode {
    ALREADY_EXISTS_LOGIN_ID(HttpStatus.CONFLICT, "ADMIN_001", "이미 사용 중인 관리자 아이디입니다."),
    MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_002", "존재하지 않는 관리자 계정입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "ADMIN_003", "비밀번호가 일치하지 않습니다."),
    DELETED_MANAGER(HttpStatus.FORBIDDEN, "ADMIN_004", "삭제된 관리자 계정입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ADMIN_005", "해당 작업을 수행할 권한이 없습니다."); // 권한 에러 추가

    private final HttpStatus status;
    private final String code;
    private final String message;
}