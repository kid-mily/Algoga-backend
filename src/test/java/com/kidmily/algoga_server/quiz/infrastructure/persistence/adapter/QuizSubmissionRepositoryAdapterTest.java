package com.kidmily.algoga_server.quiz.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.quiz.domain.model.QuizSubmission;
import com.kidmily.algoga_server.quiz.exception.QuizErrorCode;
import com.kidmily.algoga_server.quiz.exception.QuizException;
import com.kidmily.algoga_server.quiz.infrastructure.persistence.entity.QuizSubmissionJpaEntity;
import com.kidmily.algoga_server.quiz.infrastructure.persistence.repository.SpringDataQuizSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizSubmissionRepositoryAdapterTest {

    @Mock private SpringDataQuizSubmissionRepository springDataQuizSubmissionRepository;

    @InjectMocks
    private QuizSubmissionRepositoryAdapter quizSubmissionRepositoryAdapter;

    @Test
    void rejectsSubmissionWhenConcurrentRequestWinsTheInsertRace() {
        Long userId = 1L;
        Long courseId = 2L;

        // 퀴즈는 1회만 응시 가능 - QuizService의 existsByUserIdAndCourseId 체크를 통과했더라도
        // 동시 제출 경쟁으로 saveAndFlush()가 unique 제약 위반을 일으키면 QUIZ_ALREADY_SUBMITTED로 거부해야 한다.
        // (제출 저장은 QuizService가 관리하는 큰 트랜잭션 안에서, 별도 트랜잭션 없이 그대로 실행된다 -
        // 뒤이은 답안 저장이 실패해도 제출 행만 따로 커밋돼 남는 일이 없도록.)
        when(springDataQuizSubmissionRepository.saveAndFlush(any(QuizSubmissionJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException("uk_quiz_submission_user_course duplicate"));

        QuizException exception = assertThrows(
                QuizException.class,
                () -> quizSubmissionRepositoryAdapter.save(QuizSubmission.create(userId, courseId, 5, 4, 80))
        );

        assertSame(QuizErrorCode.QUIZ_ALREADY_SUBMITTED, exception.getErrorCode());
    }
}
