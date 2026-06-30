package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseReviewJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataCourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
                    existingEntity.update(
                            courseReview.getRating(),
                            courseReview.getContent(),
                            courseReview.getUpdatedAt()
                    );
                    existingEntity.updateVisibility(
                            courseReview.isDeleted(),
                            courseReview.getDeletedAt(),
                            courseReview.getUpdatedAt()
                    );

                    return existingEntity;
                })
                .orElseGet(() -> new CourseReviewJpaEntity(
                        courseReview.getCourseId(),
                        courseReview.getUserId(),
                        courseReview.getRating(),
                        courseReview.getContent(),
                        courseReview.isDeleted(),
                        courseReview.getDeletedAt(),
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
    public List<CourseReview> findAllByCourseId(Long courseId) {
        return springDataCourseReviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Map<Long, Double> findAverageRatingsByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> result = new HashMap<>();
        for (Object[] row : springDataCourseReviewRepository.findAverageRatingsByCourseIds(courseIds)) {
            Long courseId = (Long) row[0];
            Double average = ((Number) row[1]).doubleValue();
            result.put(courseId, Math.round(average * 10.0) / 10.0);
        }
        return result;
    }

    @Override
    public Set<Long> findReviewedCourseIdsByUserIdAndCourseIds(Long userId, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Set.of();
        }

        return new LinkedHashSet<>(
                springDataCourseReviewRepository.findReviewedCourseIdsByUserIdAndCourseIds(userId, courseIds)
        );
    }

    @Override
    public boolean existsByUserIdAndCourseIdAndDeletedFalse(
            Long userId,
            Long courseId
    ) {
        return springDataCourseReviewRepository.existsByUserIdAndCourseIdAndDeletedFalse(userId, courseId);
    }

    @Override
    public void deleteHiddenBefore(LocalDateTime threshold) {
        springDataCourseReviewRepository.deleteByDeletedTrueAndDeletedAtBefore(threshold);
    }

    private CourseReview toDomain(CourseReviewJpaEntity entity) {
        return CourseReview.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getUserId(),
                entity.getRating(),
                entity.getContent(),
                entity.isDeleted(),
                entity.getDeletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}


