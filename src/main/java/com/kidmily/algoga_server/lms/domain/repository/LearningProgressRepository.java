package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;

import java.util.Optional;

public interface LearningProgressRepository {

    LearningProgress save(LearningProgress learningProgress);

    Optional<LearningProgress> findByUserIdAndChapterId(Long userId, Long chapterId);

    boolean existsCompletedByUserIdAndChapterId(Long userId, Long chapterId);
}