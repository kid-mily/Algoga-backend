package com.kidmily.algoga_server.benefit.application.command;

public record RewardCourseCommand(
        Long userId,
        Long courseId
) {
}