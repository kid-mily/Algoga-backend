package com.kidmily.algoga_server.lms.exception;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// lms 패키지의 api들에서 발생하는 예외를 이 Advice가 처리
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.lms.presentation.api")
public class LmsExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    // 추가적인 도메인 특화 예외 처리가 필요하면 이곳에 작성
}