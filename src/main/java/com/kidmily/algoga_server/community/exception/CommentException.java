package com.kidmily.algoga_server.community.exception;

public class CommentException extends RuntimeException {
    private final PostErrorCode errorCode;

    public CommentException(PostErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PostErrorCode getErrorCode() {
        return errorCode;
    }
}