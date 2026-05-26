package com.kidmily.algoga_server.calendar.presentation.advice;

import com.kidmily.algoga_server.calendar.exception.CalendarErrorCode;
import com.kidmily.algoga_server.calendar.exception.CalendarException;
import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.calendar.presentation.api")
public class CalendarExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    @ExceptionHandler(CalendarException.class)
    public ResponseEntity<ErrorResponse> handleCalendarException(CalendarException e) {
        String traceId = getOrCreateTraceId();
        CalendarErrorCode errorCode = e.getErrorCode();

        log.warn("[CalendarDomainException] traceId: {}, code: {}, message: {}",
                traceId, errorCode.getCode(), errorCode.getMessage());

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}