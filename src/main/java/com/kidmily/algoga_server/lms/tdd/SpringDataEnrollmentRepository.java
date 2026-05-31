package com.kidmily.algoga_server.lms.tdd;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {
    Optional<EnrollmentJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}