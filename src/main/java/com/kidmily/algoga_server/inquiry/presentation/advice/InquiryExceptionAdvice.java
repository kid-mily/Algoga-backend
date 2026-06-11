// inquiry/presentation/advice/InquiryExceptionAdvice.java
package com.kidmily.algoga_server.inquiry.presentation.advice;

import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import com.kidmily.algoga_server.inquiry.exception.InquiryException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.inquiry.presentation.api")
public class InquiryExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    // 🌟 InquiryException이 발생하면 CommonExceptionAdvice의 기본 비즈니스 예외 처리 로직을 태웁니다.
    @ExceptionHandler(InquiryException.class)
    public ResponseEntity<ErrorResponse> handleInquiryException(InquiryException e) {
        return handleBusinessException(e);
    }
}