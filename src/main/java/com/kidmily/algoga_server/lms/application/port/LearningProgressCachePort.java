package com.kidmily.algoga_server.lms.application.port;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;

import java.util.List;
import java.util.Optional;

public interface LearningProgressCachePort {

    Optional<LearningProgress> find(Long userId, Long courseId, Long chapterId);

    void cacheClean(LearningProgress learningProgress);

    void cacheDirty(LearningProgress learningProgress);

    List<LearningProgress> findDirtyProgresses();

    void markFlushed(LearningProgress learningProgress);
}