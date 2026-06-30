package com.kidmily.algoga_server.lms.application.result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PublishedCourseListCacheResult(
        List<CourseResult> courses
) {
}