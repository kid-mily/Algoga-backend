package com.kidmily.algoga_server.lms.application.command;

public record CreateCourseCommand(
        Long countryId,
        Long managerId,
        String title,
        String description,
        Integer price,
        String thumbnailUrl,
        String fileUrl
) {
}