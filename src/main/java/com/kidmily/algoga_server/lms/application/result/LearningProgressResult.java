package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;

public record LearningProgressResult(
        Long progressId,
        Long userId,
        Long courseId,
        Long chapterId,
        int watchedSeconds,
        int progressRate,
        boolean completed,
        Long nextChapterId,
        boolean nextChapterUnlocked,
        int courseProgressRate,
        boolean quizAvailable,
        CourseClassroomResult classroom
) {
    public static LearningProgressResult of(
            LearningProgress learningProgress,
            Long nextChapterId,
            boolean nextChapterUnlocked,
            int courseProgressRate,
            boolean quizAvailable,
            CourseClassroomResult classroom
    ) {
        return new LearningProgressResult(
                learningProgress.getId(),
                learningProgress.getUserId(),
                learningProgress.getCourseId(),
                learningProgress.getChapterId(),
                learningProgress.getWatchedSeconds(),
                learningProgress.getProgressRate(),
                learningProgress.isCompleted(),
                nextChapterId,
                nextChapterUnlocked,
                courseProgressRate,
                quizAvailable,
                classroom
        );
    }
}