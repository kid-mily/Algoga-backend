package com.kidmily.algoga_server.country.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CountryErrorCode implements BaseErrorCode {
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND,"CTR_001","국가를 찾을 수없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
