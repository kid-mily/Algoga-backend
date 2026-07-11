package com.kidmily.algoga_server.learningprogress.infrastructure.redis;

import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;

public record LearningProgressCachePayload(
        Long id,
        Long userId,
        Long courseId,
        Long chapterId,
        int watchedSeconds,
        int progressRate,
        boolean completed
) {

    public static LearningProgressCachePayload from(LearningProgress learningProgress) {
        return new LearningProgressCachePayload(
                learningProgress.getId(),
                learningProgress.getUserId(),
                learningProgress.getCourseId(),
                learningProgress.getChapterId(),
                learningProgress.getWatchedSeconds(),
                learningProgress.getProgressRate(),
                learningProgress.isCompleted()
        );
    }

    public LearningProgress toDomain() {
        return LearningProgress.withId(
                id,
                userId,
                courseId,
                chapterId,
                watchedSeconds,
                progressRate,
                completed
        );
    }
}