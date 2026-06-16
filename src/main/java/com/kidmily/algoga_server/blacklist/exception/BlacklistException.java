package com.kidmily.algoga_server.blacklist.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class BlacklistException extends BusinessException {
    public BlacklistException(BlacklistErrorCode errorCode) {
        super(errorCode);
    }
}