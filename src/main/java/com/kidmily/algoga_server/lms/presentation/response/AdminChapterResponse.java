package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.Chapter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어드민 챕터 응답")
public record AdminChapterResponse(

        @Schema(description = "챕터 ID", example = "1")
        Long chapterId,

        @Schema(description = "강의 ID", example = "1")
        Long courseId,

        @Schema(description = "챕터 제목", example = "오사카 입국 준비")
        String title,

        @Schema(description = "챕터 영상 파일 경로", example = "videos/550e8400-e29b-41d4-a716-446655440000.mp4")
        String videoUrl,

        @Schema(description = "영상 재생 시간. 초 단위", example = "600")
        int durationSeconds,

        @Schema(description = "챕터 노출 순서", example = "1")
        int chapterOrder
) {

    public static AdminChapterResponse from(Chapter chapter) {
        return new AdminChapterResponse(
                chapter.getId(),
                chapter.getCourseId(),
                chapter.getTitle(),
                chapter.getVideoUrl(),
                chapter.getDurationSeconds(),
                chapter.getChapterOrder()
        );
    }
}