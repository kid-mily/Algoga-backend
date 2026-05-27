package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.Course;

public record CourseListResponse(
        Long courseId,
        Long countryId,
        String title,
        String description,
        Integer price,
        String thumbnailUrl,
        String fileUrl,
        String level,
        String levelName,
        String status
) {

    public static CourseListResponse from(Course course) {
        return new CourseListResponse(
                course.getId(),
                course.getCountryId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getThumbnailUrl(),
                course.getFileUrl(),
                course.getLevel(),
                toLevelName(course.getLevel()),
                course.getStatus()
        );
    }

    private static String toLevelName(String level) {
        return switch (level) {
            case "BEGINNER" -> "초급";
            case "INTERMEDIATE" -> "중급";
            case "ADVANCED" -> "고급";
            default -> "";
        };
    }
}