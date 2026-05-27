package com.kidmily.algoga_server.lms.application.command;

public record UpdateCourseCommand(
        String title,
        String description,
        Integer price,
        String thumbnailUrl,
        String fileUrl,
        String level
) {
}