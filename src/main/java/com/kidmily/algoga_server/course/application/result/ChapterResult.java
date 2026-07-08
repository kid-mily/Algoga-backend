package com.kidmily.algoga_server.course.application.result;

import com.kidmily.algoga_server.course.domain.model.Chapter;

public record ChapterResult(
        Long chapterId,
        Long courseId,
        String title,
        String description,
        String videoUrl,
        int durationSeconds,
        int chapterOrder
) {
    public static ChapterResult from(Chapter chapter) {
        return new ChapterResult(
                chapter.getId(),
                chapter.getCourseId(),
                chapter.getTitle(),
                chapter.getDescription(),
                chapter.getVideoUrl(),
                chapter.getDurationSeconds(),
                chapter.getChapterOrder()
        );
    }
}