package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.Enrollment;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    List<Enrollment> findByUserId(Long userId);
    List<Enrollment> findByCourseId(Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseId(Long courseId);
}
