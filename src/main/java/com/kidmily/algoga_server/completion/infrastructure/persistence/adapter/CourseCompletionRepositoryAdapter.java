package com.kidmily.algoga_server.completion.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.completion.infrastructure.persistence.entity.CourseCompletionJpaEntity;
import com.kidmily.algoga_server.completion.infrastructure.persistence.repository.SpringDataCourseCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseCompletionRepositoryAdapter implements CourseCompletionRepository {

    private final SpringDataCourseCompletionRepository springDataCourseCompletionRepository;

    @Override
    public CourseCompletion save(CourseCompletion courseCompletion) {
        CourseCompletionJpaEntity entity = new CourseCompletionJpaEntity(
                courseCompletion.getUserId(),
                courseCompletion.getCourseId(),
                courseCompletion.getCertificateCode(),
                courseCompletion.getCompletedAt()
        );

        CourseCompletionJpaEntity savedEntity = springDataCourseCompletionRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<CourseCompletion> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataCourseCompletionRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toDomain);
    }

    @Override
    public List<CourseCompletion> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        return springDataCourseCompletionRepository.findByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataCourseCompletionRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Map<Long, Long> countByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : springDataCourseCompletionRepository.countByCourseIds(courseIds)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataCourseCompletionRepository.deleteByUserId(userId);
    }

    private CourseCompletion toDomain(CourseCompletionJpaEntity entity) {
        return CourseCompletion.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getCertificateCode(),
                entity.getCompletedAt()
        );
    }
}

