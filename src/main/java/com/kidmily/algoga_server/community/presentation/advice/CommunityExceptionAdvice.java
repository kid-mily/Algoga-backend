package com.kidmily.algoga_server.community.presentation.advice;

import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.community.presentation.api")
public class CommunityExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    // 커뮤니티 도메인에서만 발생하는 특수 예외가 있다면 이곳에 추가

    @ExceptionHandler(PostException.class)
    public ResponseEntity<ErrorResponse> handlePostException(PostException e) {
        String traceId = getOrCreateTraceId();
        PostErrorCode errorCode = e.getErrorCode();

        log.warn("[PostDomainException] traceId: {}, code: {}, message: {}",
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