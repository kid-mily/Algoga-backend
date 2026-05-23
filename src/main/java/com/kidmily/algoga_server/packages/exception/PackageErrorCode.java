package com.kidmily.algoga_server.packages.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PackageErrorCode implements BaseErrorCode {

    PACKAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PKG_001", "패키지를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}