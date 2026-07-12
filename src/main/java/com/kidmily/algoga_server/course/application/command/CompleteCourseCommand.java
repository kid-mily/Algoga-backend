package com.kidmily.algoga_server.course.application.command;

public record CompleteCourseCommand(
        Long userId,
        Long courseId
) {
}