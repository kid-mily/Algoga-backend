package com.kidmily.algoga_server.global.exception;

import com.kidmily.algoga_server.global.common.api.response.AccountLockedErrorResponse;
import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.common.api.response.InvalidPasswordErrorResponse;
import com.kidmily.algoga_server.global.filter.TraceIdFilter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException; // 🔥 추가된 import

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

public interface CommonExceptionAdvice {

    Logger getLogger();

    /** 메트릭 기록이 필요한 구현체만 오버라이드 (기본값 null → 메트릭 미기록) */
    default MeterRegistry getMeterRegistry() {
        return null;
    }

    /** algoga_api_errors_total{reason=...} 카운터 증가 — Grafana "API 에러 발생률" 패널용 */
    default void recordApiError(String reason) {
        MeterRegistry registry = getMeterRegistry();
        if (registry != null) {
            registry.counter("algoga_api_errors_total", "reason", reason).increment();
        }
    }

    // 0. 인가(Authorization) 실패 — @PreAuthorize 거부 시 403으로 응답
    @ExceptionHandler(AccessDeniedException.class)
    default ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.ACCESS_DENIED;

        getLogger().warn("[AccessDeniedException] traceId: {}, message: {}", traceId, e.getMessage());
        recordApiError("access_denied");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 1-1. 계정 잠금 에러 (BusinessException보다 먼저 매칭되도록 별도 핸들러로 분리 - 잠금 해제까지 남은 시간을 함께 내려줌)
    @ExceptionHandler(AccountLockedException.class)
    default ResponseEntity<AccountLockedErrorResponse> handleAccountLockedException(AccountLockedException e) {
        String traceId = getOrCreateTraceId();
        BaseErrorCode errorCode = e.getErrorCode();

        getLogger().warn("[AccountLockedException] traceId: {}, code: {}, remainingSeconds: {}",
                traceId, errorCode.getCode(), e.getRemainingSeconds());
        recordApiError("account_locked");

        AccountLockedErrorResponse response = new AccountLockedErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId,
                e.getRemainingSeconds()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 1-2. 비밀번호 오류 (BusinessException보다 먼저 매칭되도록 별도 핸들러로 분리 - 누적 실패 횟수를 함께 내려줌)
    @ExceptionHandler(InvalidPasswordException.class)
    default ResponseEntity<InvalidPasswordErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
        String traceId = getOrCreateTraceId();
        BaseErrorCode errorCode = e.getErrorCode();

        getLogger().warn("[InvalidPasswordException] traceId: {}, code: {}, failCount: {}/{}",
                traceId, errorCode.getCode(), e.getFailCount(), e.getMaxAttempts());
        recordApiError("invalid_password");

        InvalidPasswordErrorResponse response = new InvalidPasswordErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId,
                e.getFailCount(),
                e.getMaxAttempts()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 1. 비즈니스 로직 에러
    @ExceptionHandler(BusinessException.class)
    default ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        String traceId = getOrCreateTraceId();
        BaseErrorCode errorCode = e.getErrorCode();

        getLogger().warn("[BusinessException] traceId: {}, code: {}, message: {}",
                traceId, errorCode.getCode(), errorCode.getMessage());
        recordApiError("business");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 2. @Valid 어노테이션 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    default ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST;

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        getLogger().warn("[ValidationException] traceId: {}, message: {}", traceId, errorMessage);
        recordApiError("validation");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorMessage.isEmpty() ? errorCode.getMessage() : errorMessage,
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 3. API는 존재하지만 파라미터 타입이 안 맞거나, 값이 누락되거나, JSON 구조가 잘못된 경우
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    default ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST;
        String errorMessage = errorCode.getMessage();

        if (e instanceof MethodArgumentTypeMismatchException mismatchException) {
            errorMessage = String.format("파라미터 '%s'의 타입이 올바르지 않습니다. (요청 값: %s)", mismatchException.getName(), mismatchException.getValue());
        } else if (e instanceof MissingServletRequestParameterException missingException) {
            errorMessage = String.format("필수 쿼리 파라미터 '%s'가 누락되었습니다.", missingException.getParameterName());
        } else if (e instanceof HttpMessageNotReadableException) {
            errorMessage = "요청 본문(Body)의 JSON 형식이 올바르지 않거나 데이터 타입이 일치하지 않습니다.";
        }

        getLogger().warn("[BadRequestException] traceId: {}, message: {}", traceId, errorMessage);
        recordApiError("bad_request");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorMessage,
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 4. API 경로는 일치하지만 HTTP 메서드(GET, POST 등)가 잘못된 경우
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    default ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED;

        getLogger().warn("[HttpRequestMethodNotSupportedException] traceId: {}, message: {}", traceId, e.getMessage());
        recordApiError("method_not_allowed");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 5. 요청한 API 경로가 아예 존재하지 않는 경우 (404) - 🔥 NoResourceFoundException 추가
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    default ResponseEntity<ErrorResponse> handleNotFoundException(Exception e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.API_NOT_FOUND;

        getLogger().warn("[NotFoundException] traceId: {}, message: {}", traceId, e.getMessage());
        recordApiError("not_found");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                traceId
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 6. 그 외 예상치 못한 서버 에러 최후의 보루
    @ExceptionHandler(Exception.class)
    default ResponseEntity<ErrorResponse> handleException(Exception e) {
        String traceId = getOrCreateTraceId();
        GlobalErrorCode errorCode = GlobalErrorCode.SERVER_ERROR;

        getLogger().error("[InternalServerError] traceId: {} - {}", traceId, errorCode.getMessage(), e);
        recordApiError("server_error");

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
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        if (traceId != null) {
            return traceId;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String cachedTraceId = (String) request.getAttribute(TraceIdFilter.TRACE_ID_KEY);
            if (cachedTraceId != null) {
                return cachedTraceId;
            }
        }

        return UUID.randomUUID().toString().substring(0, 8);
    }
}