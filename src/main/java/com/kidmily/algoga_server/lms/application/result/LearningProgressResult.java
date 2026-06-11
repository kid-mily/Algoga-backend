package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;

public record LearningProgressResult(
        Long progressId,
        Long userId,
        Long courseId,
        Long chapterId,
        int watchedSeconds,
        int progressRate,
        boolean completed
) {
    public static LearningProgressResult from(LearningProgress learningProgress) {
        return new LearningProgressResult(
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