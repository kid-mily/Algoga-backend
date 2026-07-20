package com.kidmily.algoga_server.quiz.domain.repository;

import com.kidmily.algoga_server.quiz.domain.model.QuizSubmission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuizSubmissionRepository {

    QuizSubmission save(QuizSubmission quizSubmission);

    Optional<QuizSubmission> findByUserIdAndCourseId(Long userId, Long courseId);

    Set<Long> findSubmittedCourseIdsByUserIdAndCourseIds(Long userId, List<Long> courseIds);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<Long> findSubmissionIdsByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
