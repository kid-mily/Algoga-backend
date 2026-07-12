package com.kidmily.algoga_server.quiz.infrastructure.persistence.repository;

import com.kidmily.algoga_server.quiz.infrastructure.persistence.entity.QuizJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataQuizRepository extends JpaRepository<QuizJpaEntity, Long> {

    List<QuizJpaEntity> findByCourseIdAndDeletedFalseOrderByIdAsc(Long courseId);

    Optional<QuizJpaEntity> findByIdAndCourseIdAndDeletedFalse(Long id, Long courseId);
}