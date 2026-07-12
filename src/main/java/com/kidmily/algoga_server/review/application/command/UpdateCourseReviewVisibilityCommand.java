package com.kidmily.algoga_server.review.application.command;

public record UpdateCourseReviewVisibilityCommand(
        Long courseId,
        Long reviewId,
        Boolean hidden
) {
}
