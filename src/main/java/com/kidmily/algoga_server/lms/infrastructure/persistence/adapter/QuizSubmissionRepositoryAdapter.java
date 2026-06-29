package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.QuizSubmissionJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataQuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class QuizSubmissionRepositoryAdapter implements QuizSubmissionRepository {

    private final SpringDataQuizSubmissionRepository springDataQuizSubmissionRepository;

    @Override
    public QuizSubmission save(QuizSubmission quizSubmission) {
        QuizSubmissionJpaEntity entity = springDataQuizSubmissionRepository
                .findByUserIdAndCourseId(
                        quizSubmission.getUserId(),
                        quizSubmission.getCourseId()
                )
                .map(existingEntity -> {
                    existingEntity.updateResult(
                            quizSubmission.getTotalCount(),
                            quizSubmission.getCorrectCount(),
                            quizSubmission.getScore(),
                            quizSubmission.getSubmittedAt()
                    );
                    return existingEntity;
                })
                .orElseGet(() -> new QuizSubmissionJpaEntity(
                        quizSubmission.getUserId(),
                        quizSubmission.getCourseId(),
                        quizSubmission.getTotalCount(),
                        quizSubmission.getCorrectCount(),
                        quizSubmission.getScore(),
                        quizSubmission.getSubmittedAt()
                ));

        QuizSubmissionJpaEntity savedEntity = springDataQuizSubmissionRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<QuizSubmission> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataQuizSubmissionRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toDomain);
    }

    @Override
    public Set<Long> findSubmittedCourseIdsByUserIdAndCourseIds(Long userId, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Set.of();
        }

        return new LinkedHashSet<>(
                springDataQuizSubmissionRepository.findSubmittedCourseIdsByUserIdAndCourseIds(userId, courseIds)
        );
    }

    @Override
    public boolean existsByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataQuizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    private QuizSubmission toDomain(QuizSubmissionJpaEntity entity) {
        return QuizSubmission.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getTotalCount(),
                entity.getCorrectCount(),
                entity.getScore(),
                entity.getSubmittedAt()
        );
    }
}

