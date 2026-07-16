package com.kidmily.algoga_server.completion.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class CompletionException extends BusinessException {

    public CompletionException(CompletionErrorCode errorCode) {
        super(errorCode);
    }
}
