package com.kidmily.algoga_server.completion.application.service;

import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseCompletionRegistrarTest {

    @Mock private CourseCompletionInsertTransactionExecutor insertTransactionExecutor;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CourseCompletionRegistrar courseCompletionRegistrar;

    @Test
    void retriesWithNewCertificateCodeOnCollision() {
        Long userId = 1L;
        Long courseId = 2L;

        CourseCompletion secondAttemptResult = CourseCompletion.withId(
                99L, userId, courseId, "ALG-2026-CERT-000002", LocalDateTime.now()
        );

        when(insertTransactionExecutor.findExisting(userId, courseId))
                .thenReturn(Optional.empty()) // 최초 조회: 완주 기록 없음
                .thenReturn(Optional.empty()); // 충돌 직후 재확인: 여전히 없음(=진짜 코드 충돌, 경쟁 아님)

        when(insertTransactionExecutor.insertNew(userId, courseId))
                .thenThrow(new DataIntegrityViolationException("certificate_code duplicate")) // 1차 시도: 코드 충돌
                .thenReturn(secondAttemptResult); // 2차 시도: 성공 (별도 REQUIRES_NEW 트랜잭션)

        CourseCompletionResult result = courseCompletionRegistrar.register(userId, courseId);

        assertEquals(secondAttemptResult.getId(), result.completionId());
        verify(insertTransactionExecutor, times(2)).insertNew(userId, courseId);
        verify(eventPublisher, times(1)).publishEvent(any(CourseCompletionCompletedEvent.class));
    }

    @Test
    void doesNotPublishDuplicateEventWhenLosingConcurrentCompletionRace() {
        Long userId = 1L;
        Long courseId = 2L;

        CourseCompletion winnerCompletion = CourseCompletion.withId(
                50L, userId, courseId, "ALG-2026-CERT-000050", LocalDateTime.now()
        );

        when(insertTransactionExecutor.findExisting(userId, courseId))
                .thenReturn(Optional.empty()) // 최초 조회: 아직 없음
                .thenReturn(Optional.of(winnerCompletion)); // 충돌 직후 재확인(새 REQUIRES_NEW 조회): 동시 요청이 먼저 만든 행 발견

        when(insertTransactionExecutor.insertNew(userId, courseId))
                .thenThrow(new DataIntegrityViolationException("uk_course_completion_user_course duplicate"));

        CourseCompletionResult result = courseCompletionRegistrar.register(userId, courseId);

        assertEquals(winnerCompletion.getId(), result.completionId());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void givesUpAfterMaxAttemptsOfRepeatedCollisions() {
        Long userId = 1L;
        Long courseId = 2L;

        when(insertTransactionExecutor.findExisting(userId, courseId))
                .thenReturn(Optional.empty());

        DataIntegrityViolationException collision = new DataIntegrityViolationException("certificate_code duplicate");
        when(insertTransactionExecutor.insertNew(userId, courseId))
                .thenThrow(collision);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> courseCompletionRegistrar.register(userId, courseId)
        );

        verify(eventPublisher, never()).publishEvent(any());
    }
}
