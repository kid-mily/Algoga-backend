package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.Course;

public record CourseListResponse(
        Long courseId,
        Long countryId,
        String title,
        String description,
        String thumbnailUrl,
        String fileUrl,
        String status
) {

    public static CourseListResponse from(Course course) {
        return new CourseListResponse(
                course.getId(),
                course.getCountryId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getFileUrl(),
                course.getStatus()
        );
    }
}