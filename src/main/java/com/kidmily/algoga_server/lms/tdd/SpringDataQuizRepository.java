package com.kidmily.algoga_server.lms.tdd;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.QuizJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataQuizRepository extends JpaRepository<QuizJpaEntity, Long> {

    List<QuizJpaEntity> findByCourseIdAndDeletedFalseOrderByIdAsc(Long courseId);

    Optional<QuizJpaEntity> findByIdAndCourseIdAndDeletedFalse(Long id, Long courseId);
}