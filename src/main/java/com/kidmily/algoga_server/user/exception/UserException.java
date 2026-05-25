package com.kidmily.algoga_server.user.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.Getter;

@Getter
public class UserException extends BusinessException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}