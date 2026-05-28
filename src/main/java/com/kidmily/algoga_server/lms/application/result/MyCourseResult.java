package com.kidmily.algoga_server.lms.application.result;

import java.time.LocalDateTime;

public record MyCourseResult(
        Long courseId,
        String title,
        String thumbnailUrl,
        Long countryId,
        String countryName,
        int totalDurationSeconds,
        long studentCount,
        double averageRating,
        int progressRate,
        int completedChapterCount,
        int totalChapterCount,
        String learningStatus,
        boolean quizSubmitted,
        boolean reviewWritten,
        boolean certificateAvailable,
        String certificateCode,
        String certificateDownloadUrl,
        LocalDateTime completedAt
) {
}