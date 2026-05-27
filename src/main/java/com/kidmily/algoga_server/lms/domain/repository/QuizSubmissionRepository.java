package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;

import java.util.Optional;

public interface QuizSubmissionRepository {

    QuizSubmission save(QuizSubmission quizSubmission);

    Optional<QuizSubmission> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}