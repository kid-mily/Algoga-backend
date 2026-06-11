package com.kidmily.algoga_server.report.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class ReportException extends BusinessException {

    public ReportException(ReportErrorCode errorCode) {
        super(errorCode);
    }
}