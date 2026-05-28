package com.kidmily.algoga_server.friend.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class FriendException extends BusinessException {
    public FriendException(FriendErrorCode errorCode) {
        super(errorCode);
    }
}