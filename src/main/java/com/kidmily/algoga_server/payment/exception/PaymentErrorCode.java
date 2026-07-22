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
    EXCEL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY_008", "엑셀 파일 생성에 실패했습니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY_009", "쿠폰을 찾을 수 없습니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "PAY_010", "이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "PAY_011", "만료된 쿠폰입니다."),
    COUPON_NOT_OWNED(HttpStatus.FORBIDDEN, "PAY_012", "본인의 쿠폰이 아닙니다."),
    INSUFFICIENT_MILEAGE(HttpStatus.BAD_REQUEST, "PAY_013", "마일리지 잔액이 부족합니다."),
    PORTONE_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "PAY_014", "결제 서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요."),
    INSTALLMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PAY_015", "이 예약은 일시불(전액) 결제만 가능합니다."),
    INVALID_PAYMENT_TYPE(HttpStatus.BAD_REQUEST, "PAY_016", "통합 결제는 예약금(DEPOSIT) 또는 일시불(FULL)만 가능합니다."),
    COURSE_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "PAY_017", "공개되지 않은 강의는 결제할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
