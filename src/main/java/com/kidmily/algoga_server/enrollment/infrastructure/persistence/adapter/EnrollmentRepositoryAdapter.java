package com.kidmily.algoga_server.enrollment.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.enrollment.infrastructure.persistence.entity.EnrollmentJpaEntity;
import com.kidmily.algoga_server.enrollment.infrastructure.persistence.repository.SpringDataEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final SpringDataEnrollmentRepository springDataEnrollmentRepository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity entity = springDataEnrollmentRepository
                .findByUserIdAndCourseId(enrollment.getUserId(), enrollment.getCourseId())
                .orElseGet(() -> new EnrollmentJpaEntity(
                        enrollment.getUserId(),
                        enrollment.getCourseId(),
                        enrollment.getStatus(),
                        enrollment.getEnrolledAt(),
                        enrollment.getCompletedAt(),
                        enrollment.getAccessExpiresAt()
                ));

        entity.updateStatus(enrollment.getStatus(), enrollment.getCompletedAt());
        return toDomain(springDataEnrollmentRepository.save(entity));
    }

    @Override
    public Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId) {
        return springDataEnrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toDomain);
    }

    @Override
    public List<Enrollment> findByUserId(Long userId) {
        return springDataEnrollmentRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Page<Enrollment> findByUserId(Long userId, Pageable pageable) {
        return springDataEnrollmentRepository.findByUserIdOrderByEnrolledAtDescIdDesc(userId, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<Enrollment> findByCourseId(Long courseId) {
        return springDataEnrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndCourseId(Long userId, Long courseId) {
        return springDataEnrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public long countByCourseId(Long courseId) {
        return springDataEnrollmentRepository.countByCourseId(courseId);
    }

    @Override
    public Map<Long, Long> countByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : springDataEnrollmentRepository.countByCourseIds(courseIds)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataEnrollmentRepository.deleteByUserId(userId);
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        return Enrollment.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getStatus(),
                entity.getEnrolledAt(),
                entity.getCompletedAt(),
                entity.getAccessExpiresAt()
        );
    }
}
