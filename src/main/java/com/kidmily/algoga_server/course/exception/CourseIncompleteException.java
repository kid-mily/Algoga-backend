package com.kidmily.algoga_server.course.exception;

import com.kidmily.algoga_server.course.application.result.CoursePublishRequirementResult;

public class CourseIncompleteException extends CourseException {

    private final CoursePublishRequirementResult data;

    public CourseIncompleteException(CoursePublishRequirementResult data) {
        super(CourseErrorCode.COURSE_INCOMPLETE);
        this.data = data;
    }

    public CoursePublishRequirementResult getData() {
        return data;
    }
}
