package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;

import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository {

    LearningProgress save(LearningProgress learningProgress);

    Optional<LearningProgress> findByUserIdAndChapterId(Long userId, Long chapterId);

    List<LearningProgress> findByUserId(Long userId);

    List<LearningProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    List<LearningProgress> findByCourseId(Long courseId);

    boolean existsCompletedByUserIdAndChapterId(Long userId, Long chapterId);
}