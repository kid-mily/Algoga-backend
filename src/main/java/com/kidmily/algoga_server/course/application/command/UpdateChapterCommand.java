package com.kidmily.algoga_server.course.application.command;

import com.kidmily.algoga_server.course.application.port.UploadFile;

public record UpdateChapterCommand(
        String title,
        String description,
        UploadFile videoFile,
        int durationSeconds,
        int chapterOrder
) {
}