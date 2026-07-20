package com.kidmily.algoga_server.learningprogress.domain.repository;

import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LearningProgressRepository {

    LearningProgress save(LearningProgress learningProgress);

    Optional<LearningProgress> findByUserIdAndChapterId(Long userId, Long chapterId);

    List<LearningProgress> findByUserId(Long userId);

    List<LearningProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    List<LearningProgress> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

    List<LearningProgress> findByCourseId(Long courseId);

    Map<Long, Integer> averageProgressRateByCourseIds(List<Long> courseIds);

    boolean existsCompletedByUserIdAndChapterId(Long userId, Long chapterId);

    void deleteAllByUserId(Long userId);
}

