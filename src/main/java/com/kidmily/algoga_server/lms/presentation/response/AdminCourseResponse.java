package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Admin course response")
public record AdminCourseResponse(
        Long courseId,
        Long countryId,
        Long managerId,
        String title,
        String description,
        Integer price,
        String thumbnailUrl,
        List<String> fileUrls,
        List<CourseFileResponse> files,
        String level,
        String levelName,
        String status
) {
    public static AdminCourseResponse from(CourseResult course) {
        return new AdminCourseResponse(
                course.courseId(),
                course.countryId(),
                course.managerId(),
                course.title(),
                course.description(),
                course.price(),
                course.thumbnailUrl(),
                course.fileUrls(),
                course.files().stream().map(CourseFileResponse::from).toList(),
                course.level(),
                course.levelName(),
                course.status()
        );
    }
}