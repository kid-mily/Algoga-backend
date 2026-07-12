package com.kidmily.algoga_server.learningprogress.application.command;

public record UpdateLearningProgressCommand(
        Long userId,
        Long courseId,
        Long chapterId,
        int watchedSeconds
) {
}