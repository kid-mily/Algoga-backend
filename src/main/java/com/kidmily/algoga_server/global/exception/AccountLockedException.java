package com.kidmily.algoga_server.global.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends BusinessException {

    private final long remainingSeconds;

    public AccountLockedException(BaseErrorCode errorCode, long remainingSeconds) {
        super(errorCode);
        this.remainingSeconds = remainingSeconds;
    }
}
