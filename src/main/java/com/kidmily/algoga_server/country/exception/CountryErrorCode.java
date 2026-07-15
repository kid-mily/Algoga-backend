package com.kidmily.algoga_server.country.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CountryErrorCode implements BaseErrorCode {

    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_006", "해당 국가를 찾을 수 없습니다."),
    CONTINENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_007", "해당 대륙의 국가를 찾을 수 없습니다."),
    INVALID_CONTINENT_CODE(HttpStatus.BAD_REQUEST, "LMS_008", "유효하지 않은 대륙 코드입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
