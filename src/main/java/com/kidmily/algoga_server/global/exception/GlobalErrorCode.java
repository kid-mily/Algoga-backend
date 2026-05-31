package com.kidmily.algoga_server.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_001", "서버 내부에서 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_002", "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_003", "지원하지 않는 HTTP 메서드입니다."),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL_004", "요청하신 API 경로를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}