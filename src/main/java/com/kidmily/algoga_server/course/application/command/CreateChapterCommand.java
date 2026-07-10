package com.kidmily.algoga_server.course.application.command;

import org.springframework.web.multipart.MultipartFile;

public record CreateChapterCommand(
        Long courseId,
        String title,
        String description,
        MultipartFile videoFile,
        int durationSeconds,
        int chapterOrder
) {
}