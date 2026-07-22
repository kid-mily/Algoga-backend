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
    PACKAGE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "BK_003", "예약 가능한 패키지가 없습니다."),
    LECTURE_NOT_COMPLETED(HttpStatus.FORBIDDEN, "BK_004", "해당 국가의 강의를 완강해야 패키지를 예약할 수 있습니다."),
    DEPARTURE_DATE_PASSED(HttpStatus.BAD_REQUEST, "BK_005", "출발일이 지난 상품은 예약하거나 결제할 수 없습니다."),
    BALANCE_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "BK_006", "출발 7일 전까지 잔금 결제가 마감되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}