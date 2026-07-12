package com.kidmily.algoga_server.learning.presentation.advice;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = {
        "com.kidmily.algoga_server.certificate.presentation.api",
        "com.kidmily.algoga_server.completion.presentation.api",
        "com.kidmily.algoga_server.country.presentation.api",
        "com.kidmily.algoga_server.course.presentation.api",
        "com.kidmily.algoga_server.diagnosis.presentation.api",
        "com.kidmily.algoga_server.learningprogress.presentation.api",
        "com.kidmily.algoga_server.qna.presentation.api",
        "com.kidmily.algoga_server.quiz.presentation.api",
        "com.kidmily.algoga_server.review.presentation.api"
})
public class LearningExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }
}