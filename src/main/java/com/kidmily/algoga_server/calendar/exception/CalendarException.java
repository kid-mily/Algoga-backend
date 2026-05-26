package com.kidmily.algoga_server.calendar.exception;

public class CalendarException extends RuntimeException {
    private final CalendarErrorCode errorCode;

    public CalendarException(CalendarErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CalendarErrorCode getErrorCode() {
        return errorCode;
    }
}