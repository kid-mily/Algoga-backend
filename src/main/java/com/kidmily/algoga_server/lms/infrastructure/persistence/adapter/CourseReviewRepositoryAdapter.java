package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseReviewJpaEntity;
import com.kidmily.algoga_server.lms.tdd.SpringDataCourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseReviewRepositoryAdapter implements CourseReviewRepository {

    private final SpringDataCourseReviewRepository springDataCourseReviewRepository;

    @Override
    public CourseReview save(CourseReview courseReview) {
        CourseReviewJpaEntity entity = springDataCourseReviewRepository.findById(
                        courseReview.getId() == null ? -1L : courseReview.getId()
                )
                .map(existingEntity -> {
                    if (courseReview.isDeleted()) {
                        existingEntity.softDelete(courseReview.getUpdatedAt());
                    } else {
                        existingEntity.update(
                                courseReview.getRating(),
                                courseReview.getContent(),
                                courseReview.getUpdatedAt()
                        );
                    }

                    return existingEntity;
                })
                .orElseGet(() -> new CourseReviewJpaEntity(
                        courseReview.getCourseId(),
                        courseReview.getUserId(),
                        courseReview.getRating(),
                        courseReview.getContent(),
                        courseReview.isDeleted(),
                        courseReview.getCreatedAt(),
                        courseReview.getUpdatedAt()
                ));

        CourseReviewJpaEntity savedEntity = springDataCourseReviewRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<CourseReview> findByIdAndCourseId(
            Long reviewId,
            Long courseId
    ) {
        return springDataCourseReviewRepository.findByIdAndCourseId(reviewId, courseId)
                .map(this::toDomain);
    }

    @Override
    public Optional<CourseReview> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataCourseReviewRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toDomain);
    }

    @Override
    public List<CourseReview> findByCourseId(Long courseId) {
        return springDataCourseReviewRepository.findByCourseIdAndDeletedFalseOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndCourseIdAndDeletedFalse(
            Long userId,
            Long courseId
    ) {
        return springDataCourseReviewRepository.existsByUserIdAndCourseIdAndDeletedFalse(userId, courseId);
    }

    private CourseReview toDomain(CourseReviewJpaEntity entity) {
        return CourseReview.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getUserId(),
                entity.getRating(),
                entity.getContent(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}