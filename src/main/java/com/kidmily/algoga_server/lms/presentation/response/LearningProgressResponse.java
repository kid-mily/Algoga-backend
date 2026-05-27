package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챕터 진도율 응답")
public record LearningProgressResponse(

        @Schema(description = "진도율 ID", example = "1")
        Long progressId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "챕터 ID", example = "4")
        Long chapterId,

        @Schema(description = "최대 시청 시간. 초 단위", example = "480")
        int watchedSeconds,

        @Schema(description = "진도율. 0부터 100 사이", example = "80")
        int progressRate,

        @Schema(description = "챕터 완료 여부", example = "false")
        boolean completed
) {

    public static LearningProgressResponse from(LearningProgress learningProgress) {
        return new LearningProgressResponse(
                learningProgress.getId(),
                learningProgress.getUserId(),
                learningProgress.getCourseId(),
                learningProgress.getChapterId(),
                learningProgress.getWatchedSeconds(),
                learningProgress.getProgressRate(),
                learningProgress.isCompleted()
        );
    }
}