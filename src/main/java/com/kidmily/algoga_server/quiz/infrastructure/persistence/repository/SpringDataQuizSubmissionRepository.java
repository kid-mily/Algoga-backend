package com.kidmily.algoga_server.quiz.infrastructure.persistence.repository;

import com.kidmily.algoga_server.quiz.infrastructure.persistence.entity.QuizSubmissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataQuizSubmissionRepository extends JpaRepository<QuizSubmissionJpaEntity, Long> {

    Optional<QuizSubmissionJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("""
            SELECT DISTINCT q.courseId
            FROM QuizSubmissionJpaEntity q
            WHERE q.userId = :userId
              AND q.courseId IN :courseIds
            """)
    List<Long> findSubmittedCourseIdsByUserIdAndCourseIds(
            @Param("userId") Long userId,
            @Param("courseIds") List<Long> courseIds
    );

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
