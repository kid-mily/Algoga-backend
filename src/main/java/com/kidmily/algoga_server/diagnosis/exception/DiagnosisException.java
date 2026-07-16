package com.kidmily.algoga_server.diagnosis.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class DiagnosisException extends BusinessException {

    public DiagnosisException(DiagnosisErrorCode errorCode) {
        super(errorCode);
    }
}
