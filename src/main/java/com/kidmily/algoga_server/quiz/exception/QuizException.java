package com.kidmily.algoga_server.quiz.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class QuizException extends BusinessException {

    public QuizException(QuizErrorCode errorCode) {
        super(errorCode);
    }
}
