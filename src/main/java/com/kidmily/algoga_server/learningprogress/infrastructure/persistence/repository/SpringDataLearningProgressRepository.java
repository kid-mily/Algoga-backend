package com.kidmily.algoga_server.learningprogress.infrastructure.persistence.repository;

import com.kidmily.algoga_server.learningprogress.infrastructure.persistence.entity.LearningProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataLearningProgressRepository extends JpaRepository<LearningProgressJpaEntity, Long> {

    Optional<LearningProgressJpaEntity> findByUserIdAndChapterId(Long userId, Long chapterId);

    List<LearningProgressJpaEntity> findByUserId(Long userId);

    List<LearningProgressJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<LearningProgressJpaEntity> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

    List<LearningProgressJpaEntity> findByCourseId(Long courseId);

    @Query(value = """
            SELECT up.lecture_id, COALESCE(ROUND(AVG(up.user_progress_rate)), 0)
            FROM (
                SELECT lecture_id, user_id, AVG(progress_rate) AS user_progress_rate
                FROM learning_progresses
                WHERE lecture_id IN (:courseIds)
                GROUP BY lecture_id, user_id
            ) up
            GROUP BY up.lecture_id
            """, nativeQuery = true)
    List<Object[]> averageProgressRateByCourseIds(@Param("courseIds") List<Long> courseIds);

    boolean existsByUserIdAndChapterIdAndCompletedTrue(Long userId, Long chapterId);

    void deleteByUserId(Long userId);
}

