package com.kidmily.algoga_server.course.application.result;

import java.time.LocalDateTime;

public record CourseStudentResult(
        Long userId,
        String name,
        String email,
        Long courseId,
        String courseTitle,
        int progressRate,
        int completedChapterCount,
        int totalChapterCount,
        String learningStatus,
        boolean quizSubmitted,
        boolean reviewWritten,
        LocalDateTime accessExpiresAt,
        LocalDateTime completedAt
) {
}
