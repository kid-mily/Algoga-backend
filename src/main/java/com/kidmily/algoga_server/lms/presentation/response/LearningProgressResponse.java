package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.LearningProgressResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 학습 진도 응답")
public record LearningProgressResponse(
        @Schema(description = "학습 진도 ID", example = "1")
        Long progressId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "챕터 ID", example = "1")
        Long chapterId,

        @Schema(description = "시청 시간. 초 단위", example = "600")
        int watchedSeconds,

        @Schema(description = "현재 챕터 진도율", example = "100")
        int progressRate,

        @Schema(description = "현재 챕터 학습 완료 여부", example = "true")
        boolean completed,

        @Schema(description = "다음 챕터 ID. 마지막 챕터면 null", example = "2")
        Long nextChapterId,

        @Schema(description = "다음 챕터 잠금 해제 여부", example = "true")
        boolean nextChapterUnlocked,

        @Schema(description = "강의 전체 진도율", example = "60")
        int courseProgressRate,

        @Schema(description = "퀴즈 응시 가능 여부", example = "false")
        boolean quizAvailable,

        @Schema(description = "진도율 반영 후 최신 강의장 상태")
        CourseClassroomResponse classroom
) {
    public static LearningProgressResponse from(LearningProgressResult learningProgress) {
        return new LearningProgressResponse(
                learningProgress.progressId(),
                learningProgress.userId(),
                learningProgress.courseId(),
                learningProgress.chapterId(),
                learningProgress.watchedSeconds(),
                learningProgress.progressRate(),
                learningProgress.completed(),
                learningProgress.nextChapterId(),
                learningProgress.nextChapterUnlocked(),
                learningProgress.courseProgressRate(),
                learningProgress.quizAvailable(),
                CourseClassroomResponse.from(learningProgress.classroom())
        );
    }
}