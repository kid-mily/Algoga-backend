package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailure;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardFailureRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CourseRewardFailureJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataCourseRewardFailureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseRewardFailureRepositoryAdapter implements CourseRewardFailureRepository {

    private final SpringDataCourseRewardFailureRepository springDataCourseRewardFailureRepository;

    @Override
    public CourseRewardFailure save(CourseRewardFailure courseRewardFailure) {
        CourseRewardFailureJpaEntity entity = courseRewardFailure.getId() == null
                ? createEntity(courseRewardFailure)
                : springDataCourseRewardFailureRepository.findById(courseRewardFailure.getId())
                .orElseGet(() -> createEntity(courseRewardFailure));

        if (courseRewardFailure.getId() != null) {
            entity.update(
                    courseRewardFailure.getStatus(),
                    courseRewardFailure.getFailureReason(),
                    courseRewardFailure.getRetryCount(),
                    courseRewardFailure.getLastFailedAt(),
                    courseRewardFailure.getResolvedAt()
            );
        }

        return toDomain(springDataCourseRewardFailureRepository.save(entity));
    }

    @Override
    public Optional<CourseRewardFailure> findById(Long failureId) {
        return springDataCourseRewardFailureRepository.findById(failureId)
                .map(this::toDomain);
    }

    @Override
    public List<CourseRewardFailure> findAllByStatus(CourseRewardFailureStatus status) {
        return springDataCourseRewardFailureRepository.findAllByStatus(status)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CourseRewardFailureJpaEntity createEntity(CourseRewardFailure failure) {
        return new CourseRewardFailureJpaEntity(
                failure.getUserId(),
                failure.getCourseId(),
                failure.getCompletionId(),
                failure.getStatus(),
                failure.getFailureReason(),
                failure.getRetryCount(),
                failure.getCreatedAt(),
                failure.getLastFailedAt(),
                failure.getResolvedAt()
        );
    }

    private CourseRewardFailure toDomain(CourseRewardFailureJpaEntity entity) {
        return CourseRewardFailure.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getCompletionId(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getRetryCount(),
                entity.getCreatedAt(),
                entity.getLastFailedAt(),
                entity.getResolvedAt()
        );
    }

    @Override
    public List<CourseRewardFailure> findRetryableFailures(int maxRetryCount) {
        return springDataCourseRewardFailureRepository.findRetryableFailures(maxRetryCount)
                .stream()
                .map(this::toDomain)
                .toList();
    }
}