package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseResult;

import java.util.List;

public record CourseListResponse(
        Long courseId,
        Long countryId,
        String title,
        String description,
        Integer price,
        String thumbnailUrl,
        List<String> fileUrls,
        List<CourseFileResponse> files,
        String level,
        String levelName,
        String status,
        boolean enrolled,
        boolean paid
) {

    public static CourseListResponse from(CourseResult course) {
        return from(course, false, false);
    }

    public static CourseListResponse from(
            CourseResult course,
            boolean enrolled,
            boolean paid
    ) {
        return new CourseListResponse(
                course.courseId(),
                course.countryId(),
                course.title(),
                course.description(),
                course.price(),
                course.thumbnailUrl(),
                course.fileUrls(),
                course.files()
                        .stream()
                        .map(CourseFileResponse::from)
                        .toList(),
                course.level(),
                course.levelName(),
                course.status(),
                enrolled,
                paid
        );
    }
}
