package com.kidmily.algoga_server.lms.application.command;

import org.springframework.web.multipart.MultipartFile;

public record CreateChapterCommand(
        Long courseId,
        String title,
        MultipartFile videoFile,
        int durationSeconds,
        int chapterOrder
) {
}