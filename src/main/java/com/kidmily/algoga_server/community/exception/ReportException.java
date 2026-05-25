package com.kidmily.algoga_server.community.exception;

public class ReportException extends RuntimeException {
    private final PostErrorCode errorCode;

    public ReportException(PostErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PostErrorCode getErrorCode() {
        return errorCode;
    }
}