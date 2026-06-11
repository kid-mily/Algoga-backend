package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.ChapterResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin chapter response")
public record AdminChapterResponse(
        Long chapterId,
        Long courseId,
        String title,
        String videoUrl,
        int durationSeconds,
        int chapterOrder
) {
    public static AdminChapterResponse from(ChapterResult chapter) {
        return new AdminChapterResponse(
                chapter.chapterId(),
                chapter.courseId(),
                chapter.title(),
                chapter.videoUrl(),
                chapter.durationSeconds(),
                chapter.chapterOrder()
        );
    }
}