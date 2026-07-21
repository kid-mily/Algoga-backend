package com.kidmily.algoga_server.quiz.application.service;

import com.kidmily.algoga_server.completion.application.service.CourseCompletionRegistrar;
import com.kidmily.algoga_server.quiz.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.quiz.application.policy.QuizAccessPolicy;
import com.kidmily.algoga_server.quiz.domain.model.Quiz;
import com.kidmily.algoga_server.quiz.domain.repository.QuizRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionAnswerRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.quiz.exception.QuizErrorCode;
import com.kidmily.algoga_server.quiz.exception.QuizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * QuizService 단위 테스트
 * - 코스당 퀴즈 등록 개수 제한(최대 5개) 검증
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizAccessPolicy quizAccessPolicy;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizSubmissionRepository quizSubmissionRepository;
    @Mock private QuizSubmissionAnswerRepository quizSubmissionAnswerRepository;
    @Mock private CourseCompletionRegistrar courseCompletionRegistrar;

    @InjectMocks
    private QuizService quizService;

    private CreateQuizCommand command(Long courseId) {
        return new CreateQuizCommand(
                courseId,
                "질문",
                "보기1",
                "보기2",
                "보기3",
                "보기4",
                1,
                "설명"
        );
    }

    @Test
    @DisplayName("코스에 등록된 퀴즈가 4개일 때 생성 요청하면 정상적으로 저장된다")
    void 퀴즈_4개_존재_시_생성_성공() {
        // given
        Long courseId = 1L;
        when(quizRepository.countByCourseId(courseId)).thenReturn(4L);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        quizService.createQuiz(command(courseId));

        // then
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @DisplayName("코스에 등록된 퀴즈가 이미 5개면 QUIZ_LIMIT_EXCEEDED 예외가 발생하고 저장되지 않는다")
    void 퀴즈_5개_존재_시_생성_실패() {
        // given
        Long courseId = 1L;
        when(quizRepository.countByCourseId(courseId)).thenReturn(5L);

        // when & then
        QuizException exception = assertThrows(QuizException.class, () -> quizService.createQuiz(command(courseId)));
        assertEquals(QuizErrorCode.QUIZ_LIMIT_EXCEEDED, exception.getErrorCode());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("코스에 등록된 퀴즈가 5개를 초과해도 QUIZ_LIMIT_EXCEEDED 예외가 발생한다")
    void 퀴즈_5개_초과_시_생성_실패() {
        // given
        Long courseId = 1L;
        when(quizRepository.countByCourseId(courseId)).thenReturn(6L);

        // when & then
        assertThrows(QuizException.class, () -> quizService.createQuiz(command(courseId)));
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("삭제되지 않은 퀴즈 개수만 제한에 반영된다 (레포지토리 조회 시 courseId 그대로 전달)")
    void 퀴즈_개수_조회_시_courseId_전달() {
        // given
        Long courseId = 42L;
        when(quizRepository.countByCourseId(courseId)).thenReturn(0L);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        quizService.createQuiz(command(courseId));

        // then
        verify(quizRepository).countByCourseId(courseId);
    }

    @Test
    @DisplayName("이미 퀴즈를 제출한 유저가 재응시하면 QUIZ_ALREADY_SUBMITTED 예외가 발생하고 채점/저장되지 않는다")
    void 퀴즈_재응시_시_거부() {
        // given
        Long userId = 1L;
        Long courseId = 1L;
        doNothing().when(quizAccessPolicy).validateEnrollment(userId, courseId);
        doNothing().when(quizAccessPolicy).validateCourseExists(courseId);
        doNothing().when(quizAccessPolicy).validateAllChaptersCompleted(userId, courseId);
        when(quizSubmissionRepository.existsByUserIdAndCourseId(userId, courseId)).thenReturn(true);

        SubmitQuizCommand command = new SubmitQuizCommand(userId, courseId, List.of());

        // when & then
        QuizException exception = assertThrows(QuizException.class, () -> quizService.submitQuiz(command));
        assertEquals(QuizErrorCode.QUIZ_ALREADY_SUBMITTED, exception.getErrorCode());
        verify(quizRepository, never()).findByCourseId(any());
        verify(quizSubmissionRepository, never()).save(any());
    }
}
