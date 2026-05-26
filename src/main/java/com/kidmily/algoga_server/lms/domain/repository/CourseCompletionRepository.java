package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;

import java.util.Optional;

public interface CourseCompletionRepository {

    CourseCompletion save(CourseCompletion courseCompletion);

    Optional<CourseCompletion> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}