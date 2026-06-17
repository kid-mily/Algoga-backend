package com.kidmily.algoga_server.lms.application.command;

public record UpdateCourseReviewVisibilityCommand(
        Long courseId,
        Long reviewId,
        Boolean hidden
) {
}
