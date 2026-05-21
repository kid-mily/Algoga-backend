package com.kidmily.algoga_server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 전역 에러 처리
public class GlobalExceptionHandler implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }
}