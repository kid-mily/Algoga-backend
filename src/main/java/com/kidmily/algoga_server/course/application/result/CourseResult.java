package com.kidmily.algoga_server.course.application.result;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.model.CourseLevel;

import java.util.List;

public record CourseResult(
        Long courseId,
        Long countryId,
        Long managerId,
        String title,
        String description,
        Integer price,
        String thumbnailUrl,
        List<String> fileUrls,
        List<CourseFileResult> files,
        String level,
        String levelName,
        String status,
        boolean deleted
) {
    public static CourseResult from(Course course) {
        return new CourseResult(
                course.getId(),
                course.getCountryId(),
                course.getManagerId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getThumbnailUrl(),
                course.getFileUrls(),
                course.getCourseFiles().stream().map(CourseFileResult::from).toList(),
                course.getLevel(),
                CourseLevel.find(course.getLevel()).map(CourseLevel::displayName).orElse(""),
                course.getStatus(),
                course.isDeleted()
        );
    }
}