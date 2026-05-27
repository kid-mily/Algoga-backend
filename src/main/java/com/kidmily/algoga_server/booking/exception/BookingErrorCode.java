package com.kidmily.algoga_server.booking.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookingErrorCode implements BaseErrorCode {

    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "BK_001", "예약을 찾을 수 없습니다."),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "BK_002", "이미 취소된 예약입니다."),
    PACKAGE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "BK_003", "예약 가능한 패키지가 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}