package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataCourseReviewRepository extends JpaRepository<CourseReviewJpaEntity, Long> {

    Optional<CourseReviewJpaEntity> findByIdAndCourseId(Long id, Long courseId);

    Optional<CourseReviewJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReviewJpaEntity> findByCourseIdAndDeletedFalseOrderByCreatedAtDesc(Long courseId);

    List<CourseReviewJpaEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("""
            SELECT r.courseId, AVG(r.rating)
            FROM CourseReviewJpaEntity r
            WHERE r.courseId IN :courseIds
              AND r.deleted = false
            GROUP BY r.courseId
            """)
    List<Object[]> findAverageRatingsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("""
            SELECT DISTINCT r.courseId
            FROM CourseReviewJpaEntity r
            WHERE r.userId = :userId
              AND r.courseId IN :courseIds
              AND r.deleted = false
            """)
    List<Long> findReviewedCourseIdsByUserIdAndCourseIds(
            @Param("userId") Long userId,
            @Param("courseIds") List<Long> courseIds
    );

    boolean existsByUserIdAndCourseIdAndDeletedFalse(Long userId, Long courseId);

    void deleteByDeletedTrueAndDeletedAtBefore(LocalDateTime threshold);
}
