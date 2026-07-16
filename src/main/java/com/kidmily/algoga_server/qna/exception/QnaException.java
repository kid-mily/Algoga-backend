package com.kidmily.algoga_server.qna.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class QnaException extends BusinessException {

    public QnaException(QnaErrorCode errorCode) {
        super(errorCode);
    }
}
