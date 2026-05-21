package com.kidmily.algoga_server.global.exception;

import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.filter.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

public interface CommonExceptionAdvice {

    // 구현체(클래스)의 @Slf4j 로거를 인터페이스로 가져오기 위한 추상 메서드
    Logger getLogger();

    @ExceptionHandler(BusinessException.class)
    default ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        String traceId = getOrCreateTraceId();
        BaseErrorCode errorCode = e.getErrorCode();

        getLogger().warn("[BusinessException] traceId: {}, code: {}, message: {}",
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    default ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST;

        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        getLogger().warn("[ValidationException] traceId: {}, message: {}", traceId, errorMessage);

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorMessage != null ? errorMessage : errorCode.getMessage(),
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    default ResponseEntity<ErrorResponse> handleException(Exception e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.SERVER_ERROR;

        getLogger().error("[InternalServerError] traceId: {} - {}", traceId, errorCode.getMessage(), e);

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    default String getOrCreateTraceId() {
        // 1. 현재 스레드의 MDC에서 가져오기
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        if (traceId != null) {
            return traceId;
        }

        // 2. Request 객체 내부에서 꺼내오기
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String cachedTraceId = (String) request.getAttribute(TraceIdFilter.TRACE_ID_KEY);
            if (cachedTraceId != null) {
                return cachedTraceId;
            }
        }

        // 3. 새로 생성
        return UUID.randomUUID().toString().substring(0, 8);
    }
}