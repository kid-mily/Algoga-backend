package com.kidmily.algoga_server.benefit.exception;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.benefit.presentation.api")
public class BenefitExceptionAdvice implements CommonExceptionAdvice {

    private final MeterRegistry meterRegistry;

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    // AccessDeniedException(403) 처리는 CommonExceptionAdvice의 공통 핸들러로 일원화됨 (GLOBAL_005)
}
