package com.kidmily.algoga_server.enrollment.domain.repository;

import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    List<Enrollment> findByUserId(Long userId);
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);
    List<Enrollment> findByCourseId(Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseId(Long courseId);
    Map<Long, Long> countByCourseIds(List<Long> courseIds);
    void deleteAllByUserId(Long userId);
}
