package com.kidmily.algoga_server.learning.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class LearningException extends BusinessException {

    public LearningException(LearningErrorCode errorCode) {
        super(errorCode);
    }
}