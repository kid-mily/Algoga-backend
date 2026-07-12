package com.kidmily.algoga_server.review.application.command;

public record CreateCourseReviewCommand(
        Long courseId,
        Long userId,
        int rating,
        String content
) {
}