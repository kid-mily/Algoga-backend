package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseCompletionJpaEntity;
import com.kidmily.algoga_server.lms.tdd.SpringDataCourseCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public boolean existsByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataCourseCompletionRepository.existsByUserIdAndCourseId(userId, courseId);
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