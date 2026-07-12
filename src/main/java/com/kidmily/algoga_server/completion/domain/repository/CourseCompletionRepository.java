package com.kidmily.algoga_server.completion.domain.repository;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourseCompletionRepository {

    CourseCompletion save(CourseCompletion courseCompletion);

    Optional<CourseCompletion> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseCompletion> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Map<Long, Long> countByCourseIds(List<Long> courseIds);
}
