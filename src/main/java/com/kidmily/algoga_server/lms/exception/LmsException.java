package com.kidmily.algoga_server.lms.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class LmsException extends BusinessException {

    public LmsException(LmsErrorCode errorCode) {
        super(errorCode);
    }
}