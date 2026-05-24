package com.kidmily.algoga_server.lms.application.command;

public record UpdateCourseCommand(
        String title,
        String description,
        String thumbnailUrl,
        String fileUrl
) {
}