package com.kidmily.algoga_server.lms.tdd;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.QuizSubmissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataQuizSubmissionRepository extends JpaRepository<QuizSubmissionJpaEntity, Long> {

    Optional<QuizSubmissionJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}