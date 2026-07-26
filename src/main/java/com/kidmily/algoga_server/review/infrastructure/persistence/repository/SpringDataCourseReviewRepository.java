package com.kidmily.algoga_server.review.infrastructure.persistence.repository;

import com.kidmily.algoga_server.review.infrastructure.persistence.entity.CourseReviewJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<CourseReviewJpaEntity> findByCourseIdAndDeletedFalseOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    Page<CourseReviewJpaEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    // 어드민 전체 강의 통합 후기 목록 검색. 각 파라미터가 null 이면 해당 조건을 건너뛴다.
    @Query(value = "SELECT r FROM CourseReviewJpaEntity r "
            + "WHERE (:courseId IS NULL OR r.courseId = :courseId) "
            + "AND (:rating IS NULL OR r.rating = :rating) "
            + "AND (:hidden IS NULL OR r.deleted = :hidden) "
            + "AND (:keyword IS NULL OR r.content LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM CourseReviewJpaEntity r "
            + "WHERE (:courseId IS NULL OR r.courseId = :courseId) "
            + "AND (:rating IS NULL OR r.rating = :rating) "
            + "AND (:hidden IS NULL OR r.deleted = :hidden) "
            + "AND (:keyword IS NULL OR r.content LIKE CONCAT('%', :keyword, '%'))")
    Page<CourseReviewJpaEntity> searchForAdmin(
            @Param("courseId") Long courseId,
            @Param("rating") Integer rating,
            @Param("hidden") Boolean hidden,
            @Param("keyword") String keyword,
            Pageable pageable
    );

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
