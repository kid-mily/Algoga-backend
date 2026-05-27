package com.kidmily.algoga_server.benefit.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class BenefitException extends BusinessException {

    public BenefitException(BenefitErrorCode errorCode) {
        super(errorCode);
    }
}