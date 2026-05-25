package com.kidmily.algoga_server.notice.presentation.advice;

import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.exception.NoticeException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.notice.presentation.api")
public class NoticeExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    // 🔥 공지사항 도메인 전용 예외 핸들러
    @ExceptionHandler(NoticeException.class)
    public ResponseEntity<ErrorResponse> handleNoticeException(NoticeException e) {
        String traceId = getOrCreateTraceId();
        NoticeErrorCode errorCode = e.getErrorCode();

        // WARN 로그와 함께 TraceId 기록
        log.warn("[NoticeDomainException] traceId: {}, code: {}, message: {}",
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