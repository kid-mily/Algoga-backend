package com.kidmily.algoga_server.passport.presentation.advice;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// Passport 도메인 컨트롤러에서 발생하는 예외만 처리하도록 범위 지정
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.passport.presentation.api")
public class PassportExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    // 기본적인 BusinessException, Validation 등은 부모 인터페이스의 default 메서드가 모두 처리한다.
}
