package com.kidmily.algoga_server.lms.application.command;

public record RewardCourseCommand(
        Long userId,
        Long courseId
) {
}