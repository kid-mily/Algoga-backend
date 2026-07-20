package com.kidmily.algoga_server.course.exception;

import com.kidmily.algoga_server.global.exception.BusinessException;

public class CourseException extends BusinessException {

    public CourseException(CourseErrorCode errorCode) {
        super(errorCode);
    }
}
