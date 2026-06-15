package com.kidmily.algoga_server.lms.application.result;

public record CourseClassroomChapterResult(
        Long chapterId,
        String title,
        String videoUrl,
        int durationSeconds,
        int chapterOrder,
        int watchedSeconds,
        int progressRate,
        boolean completed,
        boolean locked
) {
}
