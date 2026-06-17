package com.kidmily.algoga_server.benefit.exception;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.benefit.presentation.api")
public class BenefitExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        String traceId = getOrCreateTraceId();

        log.warn("[AccessDenied] traceId: {}, message: {}", traceId, e.getMessage());

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "GLOBAL_005",
                "접근 권한이 없습니다.",
                traceId
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
