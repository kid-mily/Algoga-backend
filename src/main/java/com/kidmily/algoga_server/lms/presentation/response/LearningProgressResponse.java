package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.LearningProgressResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Learning progress response")
public record LearningProgressResponse(
        Long progressId,
        Long userId,
        Long courseId,
        Long chapterId,
        int watchedSeconds,
        int progressRate,
        boolean completed
) {
    public static LearningProgressResponse from(LearningProgressResult learningProgress) {
        return new LearningProgressResponse(
                learningProgress.progressId(),
                learningProgress.userId(),
                learningProgress.courseId(),
                learningProgress.chapterId(),
                learningProgress.watchedSeconds(),
                learningProgress.progressRate(),
                learningProgress.completed()
        );
    }
}