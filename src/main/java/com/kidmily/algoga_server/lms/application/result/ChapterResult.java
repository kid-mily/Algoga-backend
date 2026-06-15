package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.Chapter;

public record ChapterResult(
        Long chapterId,
        Long courseId,
        String title,
        String videoUrl,
        int durationSeconds,
        int chapterOrder
) {
    public static ChapterResult from(Chapter chapter) {
        return new ChapterResult(
                chapter.getId(),
                chapter.getCourseId(),
                chapter.getTitle(),
                chapter.getVideoUrl(),
                chapter.getDurationSeconds(),
                chapter.getChapterOrder()
        );
    }
}