package com.kidmily.algoga_server.course.application.command;

import com.kidmily.algoga_server.course.application.port.UploadFile;

public record CreateChapterCommand(
        Long courseId,
        String title,
        String description,
        UploadFile videoFile,
        int durationSeconds,
        int chapterOrder
) {
}