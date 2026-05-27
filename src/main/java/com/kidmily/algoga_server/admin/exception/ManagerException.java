package com.kidmily.algoga_server.admin.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class ManagerException extends BusinessException {
    public ManagerException(ManagerErrorCode errorCode) {
        super(errorCode);
    }
}