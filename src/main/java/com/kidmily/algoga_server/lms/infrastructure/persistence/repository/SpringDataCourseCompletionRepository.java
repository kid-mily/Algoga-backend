package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseCompletionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseCompletionRepository extends JpaRepository<CourseCompletionJpaEntity, Long> {

    Optional<CourseCompletionJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseCompletionJpaEntity> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
