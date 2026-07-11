package com.kidmily.algoga_server.course.application.result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kidmily.algoga_server.course.application.result.CourseResult;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PublishedCourseListCacheResult(
        List<CourseResult> courses
) {
}