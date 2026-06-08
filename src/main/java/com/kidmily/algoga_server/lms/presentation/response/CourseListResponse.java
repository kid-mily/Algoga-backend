package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseLevel;

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

    public static CourseListResponse from(Course course) {
        return from(course, false, false);
    }

    public static CourseListResponse from(
            Course course,
            boolean enrolled,
            boolean paid
    ) {
        return new CourseListResponse(
                course.getId(),
                course.getCountryId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getThumbnailUrl(),
                course.getFileUrls(),
                course.getCourseFiles()
                        .stream()
                        .map(CourseFileResponse::from)
                        .toList(),
                course.getLevel(),
                toLevelName(course.getLevel()),
                course.getStatus(),
                enrolled,
                paid
        );
    }

    private static String toLevelName(String level) {
        return CourseLevel.find(level)
                .map(CourseLevel::displayName)
                .orElse("");
    }
}
