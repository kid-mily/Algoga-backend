package com.kidmily.algoga_server.enrollment.infrastructure.persistence.repository;

import com.kidmily.algoga_server.enrollment.infrastructure.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kidmily.algoga_server.enrollment.domain.model.EnrollmentStatus;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {
    Optional<EnrollmentJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);
    List<EnrollmentJpaEntity> findByUserId(Long userId);
    Page<EnrollmentJpaEntity> findByUserIdOrderByEnrolledAtDescIdDesc(Long userId, Pageable pageable);
    List<EnrollmentJpaEntity> findByCourseId(Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseId(Long courseId);
    void deleteByUserId(Long userId);

    @Query("""
            SELECT e.courseId, COUNT(e.id)
            FROM EnrollmentJpaEntity e
            WHERE e.courseId IN :courseIds
            GROUP BY e.courseId
            """)
    List<Object[]> countByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("""
        SELECT e FROM EnrollmentJpaEntity e
        WHERE e.status = :status
        AND e.accessExpiresAt >= :start
        AND e.accessExpiresAt < :end
        """)
    List<EnrollmentJpaEntity> findExpiringBetween(
            @Param("status") EnrollmentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
