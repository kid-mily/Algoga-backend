package com.kidmily.algoga_server.lms.domain.model;

public class LearningProgress {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final Long chapterId;
    private final int watchedSeconds;
    private final int progressRate;
    private final boolean completed;

    private LearningProgress(
            Long id,
            Long userId,
            Long courseId,
            Long chapterId,
            int watchedSeconds,
            int progressRate,
            boolean completed
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.chapterId = chapterId;
        this.watchedSeconds = watchedSeconds;
        this.progressRate = progressRate;
        this.completed = completed;
    }

    public static LearningProgress create(
            Long userId,
            Long courseId,
            Long chapterId,
            int watchedSeconds,
            int durationSeconds
    ) {
        int safeWatchedSeconds = calculateSafeWatchedSeconds(watchedSeconds, durationSeconds);
        int progressRate = calculateProgressRate(safeWatchedSeconds, durationSeconds);

        return new LearningProgress(
                null,
                userId,
                courseId,
                chapterId,
                safeWatchedSeconds,
                progressRate,
                progressRate >= 100
        );
    }

    public static LearningProgress withId(
            Long id,
            Long userId,
            Long courseId,
            Long chapterId,
            int watchedSeconds,
            int progressRate,
            boolean completed
    ) {
        return new LearningProgress(
                id,
                userId,
                courseId,
                chapterId,
                watchedSeconds,
                progressRate,
                completed
        );
    }

    public LearningProgress updateWatchedSeconds(
            int newWatchedSeconds,
            int durationSeconds
    ) {
        int safeNewWatchedSeconds = calculateSafeWatchedSeconds(newWatchedSeconds, durationSeconds);

        // 되감기하거나 더 낮은 시청 시간이 들어와도 기존 최대 시청 시간보다 줄어들지 않게 처리
        int maxWatchedSeconds = Math.max(this.watchedSeconds, safeNewWatchedSeconds);
        int newProgressRate = calculateProgressRate(maxWatchedSeconds, durationSeconds);

        return new LearningProgress(
                this.id,
                this.userId,
                this.courseId,
                this.chapterId,
                maxWatchedSeconds,
                newProgressRate,
                newProgressRate >= 100
        );
    }

    private static int calculateSafeWatchedSeconds(
            int watchedSeconds,
            int durationSeconds
    ) {
        if (watchedSeconds < 0) {
            return 0;
        }

        return Math.min(watchedSeconds, durationSeconds);
    }

    private static int calculateProgressRate(
            int watchedSeconds,
            int durationSeconds
    ) {
        if (durationSeconds <= 0) {
            return 0;
        }

        return Math.min(100, (int) Math.floor((watchedSeconds * 100.0) / durationSeconds));
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public int getWatchedSeconds() {
        return watchedSeconds;
    }

    public int getProgressRate() {
        return progressRate;
    }

    public boolean isCompleted() {
        return completed;
    }
}