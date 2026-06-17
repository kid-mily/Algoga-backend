package com.kidmily.algoga_server.lms.exception;

import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.lms.presentation.api")
public class LmsExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception e) {
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
