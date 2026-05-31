package com.kidmily.algoga_server.lms.tdd;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseReviewRepository extends JpaRepository<CourseReviewJpaEntity, Long> {

    Optional<CourseReviewJpaEntity> findByIdAndCourseId(Long id, Long courseId);

    Optional<CourseReviewJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReviewJpaEntity> findByCourseIdAndDeletedFalseOrderByCreatedAtDesc(Long courseId);

    boolean existsByUserIdAndCourseIdAndDeletedFalse(Long userId, Long courseId);
}