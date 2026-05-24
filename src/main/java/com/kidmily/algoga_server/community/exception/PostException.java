package com.kidmily.algoga_server.community.exception;

public class PostException extends RuntimeException {
    private final PostErrorCode errorCode;

    public PostException(PostErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PostErrorCode getErrorCode() {
        return errorCode;
    }
}