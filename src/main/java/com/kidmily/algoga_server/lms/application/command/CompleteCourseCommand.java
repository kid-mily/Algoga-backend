package com.kidmily.algoga_server.lms.application.command;

public record CompleteCourseCommand(
        Long userId,
        Long courseId
) {
}