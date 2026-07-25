package com.kidmily.algoga_server.completion.infrastructure.persistence.repository;

import com.kidmily.algoga_server.completion.infrastructure.persistence.entity.CourseCompletionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseCompletionRepository extends JpaRepository<CourseCompletionJpaEntity, Long> {

    Optional<CourseCompletionJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseCompletionJpaEntity> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

    List<CourseCompletionJpaEntity> findByCourseId(Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    @Query("""
            SELECT c.courseId, COUNT(DISTINCT c.userId)
            FROM CourseCompletionJpaEntity c
            WHERE c.courseId IN :courseIds
              AND EXISTS (
                  SELECT 1
                  FROM EnrollmentJpaEntity e
                  WHERE e.courseId = c.courseId
                    AND e.userId = c.userId
              )
            GROUP BY c.courseId
            """)
    List<Object[]> countByCourseIds(@Param("courseIds") List<Long> courseIds);

    void deleteByUserId(Long userId);
}
