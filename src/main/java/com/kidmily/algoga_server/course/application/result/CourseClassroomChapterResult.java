package com.kidmily.algoga_server.course.application.result;

public record CourseClassroomChapterResult(
        Long chapterId,
        String title,
        String description,
        String videoUrl,
        int durationSeconds,
        int chapterOrder,
        int watchedSeconds,
        int progressRate,
        boolean completed,
        boolean locked
) {
}