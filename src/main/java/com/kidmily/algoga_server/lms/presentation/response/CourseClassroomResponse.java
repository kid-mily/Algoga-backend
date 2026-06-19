package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseClassroomChapterResult;
import com.kidmily.algoga_server.lms.application.result.CourseClassroomResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "수강생 강의실 상세 응답")
public record CourseClassroomResponse(
        @Schema(description = "강의 ID", example = "76")
        Long courseId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        String title,

        @Schema(description = "수강 가능 만료 일시", example = "2026-12-15T10:00:00")
        LocalDateTime accessExpiresAt,

        @Schema(description = "퀴즈 응시 가능 여부", example = "false")
        boolean quizAvailable,

        @Schema(description = "챕터 학습 목록")
        List<ChapterLearningResponse> chapters
) {
    public static CourseClassroomResponse from(CourseClassroomResult result) {
        return new CourseClassroomResponse(
                result.courseId(),
                result.title(),
                result.accessExpiresAt(),
                result.quizAvailable(),
                result.chapters().stream().map(ChapterLearningResponse::from).toList()
        );
    }

    @Schema(description = "수강생 챕터 학습 응답")
    public record ChapterLearningResponse(
            @Schema(description = "챕터 ID", example = "1")
            Long chapterId,

            @Schema(description = "챕터 제목", example = "1강. 여행 전 필수 준비")
            String title,

            @Schema(description = "챕터 설명", example = "이 챕터에서 배울 내용을 입력합니다.")
            String description,

            @Schema(description = "영상 URL. 잠긴 챕터는 null")
            String videoUrl,

            @Schema(description = "영상 길이. 초 단위", example = "700")
            int durationSeconds,

            @Schema(description = "챕터 순서", example = "1")
            int chapterOrder,

            @Schema(description = "최대 시청 시간. 초 단위", example = "350")
            int watchedSeconds,

            @Schema(description = "학습 진도율", example = "50")
            int progressRate,

            @Schema(description = "챕터 완료 여부", example = "false")
            boolean completed,

            @Schema(description = "챕터 잠금 여부", example = "false")
            boolean locked
    ) {
        public static ChapterLearningResponse from(CourseClassroomChapterResult result) {
            return new ChapterLearningResponse(
                    result.chapterId(),
                    result.title(),
                    result.description(),
                    result.videoUrl(),
                    result.durationSeconds(),
                    result.chapterOrder(),
                    result.watchedSeconds(),
                    result.progressRate(),
                    result.completed(),
                    result.locked()
            );
        }
    }
}
