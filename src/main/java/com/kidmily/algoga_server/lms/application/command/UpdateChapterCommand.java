package com.kidmily.algoga_server.lms.application.command;

public record UpdateChapterCommand(
        String title,
        String videoUrl,
        int durationSeconds,
        int chapterOrder
) {
}