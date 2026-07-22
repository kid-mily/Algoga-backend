package com.kidmily.algoga_server.course.exception;

import com.kidmily.algoga_server.course.presentation.response.CourseIncompleteErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.course.presentation.api")
public class CourseExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    @ExceptionHandler(CourseIncompleteException.class)
    public ResponseEntity<CourseIncompleteErrorResponse> handleCourseIncompleteException(CourseIncompleteException e) {
        String traceId = getOrCreateTraceId();
        CourseErrorCode errorCode = CourseErrorCode.COURSE_INCOMPLETE;

        log.warn("[CourseIncompleteException] traceId: {}, data: {}", traceId, e.getData());
        recordApiError("course_incomplete");

        CourseIncompleteErrorResponse response = new CourseIncompleteErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                traceId,
                e.getData()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
