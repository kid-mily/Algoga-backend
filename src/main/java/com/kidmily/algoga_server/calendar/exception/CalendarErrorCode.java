package com.kidmily.algoga_server.calendar.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements BaseErrorCode {

    CALENDAR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "CALENDAR_001", "캘린더 조회 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}