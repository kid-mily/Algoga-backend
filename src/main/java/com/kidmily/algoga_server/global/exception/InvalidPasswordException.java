package com.kidmily.algoga_server.global.exception;

import lombok.Getter;

@Getter
public class InvalidPasswordException extends BusinessException {

    private final int failCount;
    private final int maxAttempts;

    public InvalidPasswordException(BaseErrorCode errorCode, int failCount, int maxAttempts) {
        super(errorCode);
        this.failCount = failCount;
        this.maxAttempts = maxAttempts;
    }
}
