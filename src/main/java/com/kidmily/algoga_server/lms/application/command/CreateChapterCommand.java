package com.kidmily.algoga_server.lms.application.command;

public record CreateChapterCommand(
        Long courseId,
        String title,
        String videoUrl,
        int durationSeconds,
        int chapterOrder
) {
}