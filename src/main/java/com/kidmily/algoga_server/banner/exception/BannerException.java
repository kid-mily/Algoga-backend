package com.kidmily.algoga_server.banner.exception;

import lombok.Getter;

@Getter
public class BannerException extends RuntimeException {

    private final BannerErrorCode errorCode;

    public BannerException(BannerErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}