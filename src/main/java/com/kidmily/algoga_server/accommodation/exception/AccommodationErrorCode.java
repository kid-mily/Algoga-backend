package com.kidmily.algoga_server.accommodation.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AccommodationErrorCode implements BaseErrorCode {

    ACCOMMODATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ACC_001", "숙소를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}