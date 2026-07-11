package com.kidmily.algoga_server.enrollment.infrastructure.persistence.repository;

import com.kidmily.algoga_server.enrollment.infrastructure.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {
    Optional<EnrollmentJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);
    List<EnrollmentJpaEntity> findByUserId(Long userId);
    Page<EnrollmentJpaEntity> findByUserIdOrderByEnrolledAtDescIdDesc(Long userId, Pageable pageable);
    List<EnrollmentJpaEntity> findByCourseId(Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseId(Long courseId);

    @Query("""
            SELECT e.courseId, COUNT(e.id)
            FROM EnrollmentJpaEntity e
            WHERE e.courseId IN :courseIds
            GROUP BY e.courseId
            """)
    List<Object[]> countByCourseIds(@Param("courseIds") List<Long> courseIds);
}
