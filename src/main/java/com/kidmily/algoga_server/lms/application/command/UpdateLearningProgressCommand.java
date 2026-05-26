package com.kidmily.algoga_server.lms.application.command;

public record UpdateLearningProgressCommand(
        Long userId,
        Long courseId,
        Long chapterId,
        int watchedSeconds
) {
}