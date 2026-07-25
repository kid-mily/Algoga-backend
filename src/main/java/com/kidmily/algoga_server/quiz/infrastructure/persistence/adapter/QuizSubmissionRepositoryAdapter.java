package com.kidmily.algoga_server.quiz.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.quiz.domain.model.QuizSubmission;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.quiz.exception.QuizErrorCode;
import com.kidmily.algoga_server.quiz.exception.QuizException;
import com.kidmily.algoga_server.quiz.infrastructure.persistence.entity.QuizSubmissionJpaEntity;
import com.kidmily.algoga_server.quiz.infrastructure.persistence.repository.SpringDataQuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class QuizSubmissionRepositoryAdapter implements QuizSubmissionRepository {

    private final SpringDataQuizSubmissionRepository springDataQuizSubmissionRepository;

    /**
     * 퀴즈는 1회만 응시 가능(재응시 불가) - 이미 제출한 기록이 있으면 거부한다.
     * QuizService가 호출 전에 existsByUserIdAndCourseId로 이미 걸러내지만, 동시 제출 경쟁으로
     * 그 체크를 통과한 뒤 삽입이 unique 제약(uk_quiz_submission_user_course) 위반으로 실패하는
     * 경우도 같은 QUIZ_ALREADY_SUBMITTED로 처리한다.
     * <p>
     * QuizService.submitQuiz()는 "제출 저장 -> 답안 상세 저장 -> 수료 처리"를 한 트랜잭션으로 묶고 있어서,
     * 여기서 REQUIRES_NEW로 제출 행만 먼저 커밋해버리면 뒤이은 답안 저장이 실패했을 때
     * "제출은 됐는데 답안은 없는" 불일치 상태가 남는다. 그래서 별도 트랜잭션을 열지 않고 같은
     * 트랜잭션 안에서 saveAndFlush()만 써서 unique 충돌을 즉시 감지한다 - 이러면 뒤에서 무엇이
     * 실패하든 전체가 한 번에 롤백된다(성공한 제출 행이 답안 없이 혼자 남는 일이 없다).
     */
    @Override
    public QuizSubmission save(QuizSubmission quizSubmission) {
        try {
            return toDomain(springDataQuizSubmissionRepository.saveAndFlush(newEntity(quizSubmission)));
        } catch (DataIntegrityViolationException e) {
            throw new QuizException(QuizErrorCode.QUIZ_ALREADY_SUBMITTED);
        }
    }

    private QuizSubmissionJpaEntity newEntity(QuizSubmission quizSubmission) {
        return new QuizSubmissionJpaEntity(
                quizSubmission.getUserId(),
                quizSubmission.getCourseId(),
                quizSubmission.getTotalCount(),
                quizSubmission.getCorrectCount(),
                quizSubmission.getScore(),
                quizSubmission.getSubmittedAt()
        );
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
    public Set<Long> findSubmittedUserIdsByCourseId(Long courseId) {
        return new LinkedHashSet<>(
                springDataQuizSubmissionRepository.findSubmittedUserIdsByCourseId(courseId)
        );
    }

    @Override
    public boolean existsByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataQuizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public List<Long> findSubmissionIdsByUserId(Long userId) {
        return springDataQuizSubmissionRepository.findIdsByUserId(userId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataQuizSubmissionRepository.deleteByUserId(userId);
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

