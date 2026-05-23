package com.kidmily.algoga_server.community.presentation.advice;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.community.presentation.api")
public class CommunityExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    // 커뮤니티 도메인에서만 발생하는 특수 예외가 있다면 이곳에 추가
}