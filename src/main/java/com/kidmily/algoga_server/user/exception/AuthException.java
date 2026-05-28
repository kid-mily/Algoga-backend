package com.kidmily.algoga_server.user.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;

public class AuthException extends BusinessException {
    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}