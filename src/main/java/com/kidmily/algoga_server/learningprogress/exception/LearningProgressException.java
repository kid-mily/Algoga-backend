package com.kidmily.algoga_server.learningprogress.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class LearningProgressException extends BusinessException {

    public LearningProgressException(LearningProgressErrorCode errorCode) {
        super(errorCode);
    }
}
