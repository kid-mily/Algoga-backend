package com.kidmily.algoga_server.certificate.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class CertificateException extends BusinessException {

    public CertificateException(CertificateErrorCode errorCode) {
        super(errorCode);
    }
}
