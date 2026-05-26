package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.LearningProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataLearningProgressRepository extends JpaRepository<LearningProgressJpaEntity, Long> {

    Optional<LearningProgressJpaEntity> findByUserIdAndChapterId(Long userId, Long chapterId);

    boolean existsByUserIdAndChapterIdAndCompletedTrue(Long userId, Long chapterId);
}