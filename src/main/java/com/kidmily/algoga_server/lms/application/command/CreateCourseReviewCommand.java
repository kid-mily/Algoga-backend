package com.kidmily.algoga_server.lms.application.command;

public record CreateCourseReviewCommand(
        Long courseId,
        Long userId,
        int rating,
        String content
) {
}