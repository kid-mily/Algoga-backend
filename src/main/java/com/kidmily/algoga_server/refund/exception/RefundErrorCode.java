package com.kidmily.algoga_server.refund.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RefundErrorCode implements BaseErrorCode {

    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "REF_001", "환불 요청을 찾을 수 없습니다."),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "REF_002", "예약 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "REF_003", "결제 정보를 찾을 수 없습니다."),
    ALREADY_REFUND_REQUESTED(HttpStatus.CONFLICT, "REF_004", "이미 환불 요청된 예약입니다."),
    INVALID_REFUND_STATUS(HttpStatus.BAD_REQUEST, "REF_005", "현재 상태에서는 처리할 수 없습니다."),
    BOOKING_NOT_CANCELLED(HttpStatus.BAD_REQUEST, "REF_006", "취소된 예약만 환불 요청이 가능합니다."),
    DEPOSIT_ONLY_NOT_REFUNDABLE(HttpStatus.BAD_REQUEST, "REF_007", "예약금(계약금)과 강의는 환불되지 않습니다. 잔금까지 결제된 예약만 환불할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}