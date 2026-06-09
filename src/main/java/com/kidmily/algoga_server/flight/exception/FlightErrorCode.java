package com.kidmily.algoga_server.flight.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FlightErrorCode implements BaseErrorCode {

    FLIGHT_API_ERROR(HttpStatus.BAD_GATEWAY, "FLT_001", "항공편 조회 중 오류가 발생했습니다."),
    FLIGHT_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "FLT_002", "항공편 조회 서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
