package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.Enrollment;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.EnrollmentJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public boolean existsByUserIdAndCourseId(Long userId, Long courseId) {
        return springDataEnrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
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
