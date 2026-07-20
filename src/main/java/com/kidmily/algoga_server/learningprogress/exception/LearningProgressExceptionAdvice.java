package com.kidmily.algoga_server.learningprogress.exception;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.learningprogress.presentation.api")
public class LearningProgressExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }
}
