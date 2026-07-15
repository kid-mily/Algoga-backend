package com.kidmily.algoga_server.certificate.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CertificateErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    COURSE_COMPLETION_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_019", "강의 이수 내역을 찾을 수 없습니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
