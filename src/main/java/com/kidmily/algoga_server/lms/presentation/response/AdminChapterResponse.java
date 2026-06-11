package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.ChapterResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 챕터 응답")
public record AdminChapterResponse(
        @Schema(description = "챕터 ID", example = "1")
        Long chapterId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "챕터 제목", example = "출국 전 준비사항")
        String title,

        @Schema(description = "강의 영상 URL")
        String videoUrl,

        @Schema(description = "영상 길이. 초 단위", example = "600")
        int durationSeconds,

        @Schema(description = "챕터 순서", example = "1")
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
