package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {
    Optional<EnrollmentJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);
    List<EnrollmentJpaEntity> findByUserId(Long userId);
    List<EnrollmentJpaEntity> findByCourseId(Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseId(Long courseId);
}
