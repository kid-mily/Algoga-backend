package com.kidmily.algoga_server.payment.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY_001", "결제 정보를 찾을 수 없습니다."),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY_002", "예약 정보를 찾을 수 없습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "PAY_003", "결제 금액이 올바르지 않습니다."),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "PAY_004", "이미 처리된 결제입니다."),
    PORTONE_API_ERROR(HttpStatus.BAD_GATEWAY, "PAY_005", "결제 처리 중 오류가 발생했습니다."),
    INVALID_WEBHOOK(HttpStatus.BAD_REQUEST, "PAY_006", "유효하지 않은 웹훅 요청입니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY_007", "강의 정보를 찾을 수 없습니다."),
    EXCEL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY_008", "엑셀 파일 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}