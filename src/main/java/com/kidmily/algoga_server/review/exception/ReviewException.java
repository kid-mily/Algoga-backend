package com.kidmily.algoga_server.review.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class ReviewException extends BusinessException {

    public ReviewException(ReviewErrorCode errorCode) {
        super(errorCode);
    }
}
