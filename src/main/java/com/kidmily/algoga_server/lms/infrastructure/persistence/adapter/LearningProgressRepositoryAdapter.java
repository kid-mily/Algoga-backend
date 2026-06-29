package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.LearningProgressJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataLearningProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LearningProgressRepositoryAdapter implements LearningProgressRepository {

    private final SpringDataLearningProgressRepository springDataLearningProgressRepository;

    @Override
    public LearningProgress save(LearningProgress learningProgress) {
        LearningProgressJpaEntity entity = springDataLearningProgressRepository
                .findByUserIdAndChapterId(
                        learningProgress.getUserId(),
                        learningProgress.getChapterId()
                )
                .map(existingEntity -> {
                    existingEntity.updateProgress(
                            learningProgress.getWatchedSeconds(),
                            learningProgress.getProgressRate(),
                            learningProgress.isCompleted()
                    );
                    return existingEntity;
                })
                .orElseGet(() -> new LearningProgressJpaEntity(
                        learningProgress.getUserId(),
                        learningProgress.getCourseId(),
                        learningProgress.getChapterId(),
                        learningProgress.getWatchedSeconds(),
                        learningProgress.getProgressRate(),
                        learningProgress.isCompleted()
                ));

        LearningProgressJpaEntity savedEntity = springDataLearningProgressRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<LearningProgress> findByUserIdAndChapterId(
            Long userId,
            Long chapterId
    ) {
        return springDataLearningProgressRepository.findByUserIdAndChapterId(userId, chapterId)
                .map(this::toDomain);
    }

    @Override
    public List<LearningProgress> findByUserId(Long userId) {
        return springDataLearningProgressRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<LearningProgress> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataLearningProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<LearningProgress> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        return springDataLearningProgressRepository.findByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<LearningProgress> findByCourseId(Long courseId) {
        return springDataLearningProgressRepository.findByCourseId(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsCompletedByUserIdAndChapterId(
            Long userId,
            Long chapterId
    ) {
        return springDataLearningProgressRepository.existsByUserIdAndChapterIdAndCompletedTrue(
                userId,
                chapterId
        );
    }

    private LearningProgress toDomain(LearningProgressJpaEntity entity) {
        return LearningProgress.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getChapterId(),
                entity.getWatchedSeconds(),
                entity.getProgressRate(),
                entity.isCompleted()
        );
    }
}

