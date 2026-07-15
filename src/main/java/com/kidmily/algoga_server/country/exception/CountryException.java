package com.kidmily.algoga_server.country.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class CountryException extends BusinessException {

    public CountryException(CountryErrorCode errorCode) {
        super(errorCode);
    }
}
