package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.RecordCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailure;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardFailureRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRewardFailureServiceTest {

    private static final Long FAILURE_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long COURSE_ID = 57L;
    private static final Long COMPLETION_ID = 3L;

    @Mock
    private CourseRewardFailureRepository courseRewardFailureRepository;

    @Mock
    private CourseRewardUseCase courseRewardUseCase;

    @InjectMocks
    private CourseRewardFailureService courseRewardFailureService;

    @Test
    void recordsRewardFailureAsPending() {
        when(courseRewardFailureRepository.save(any(CourseRewardFailure.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        courseRewardFailureService.recordFailure(
                new RecordCourseRewardFailureCommand(
                        USER_ID,
                        COURSE_ID,
                        COMPLETION_ID,
                        "reward failed"
                )
        );

        ArgumentCaptor<CourseRewardFailure> captor = ArgumentCaptor.forClass(CourseRewardFailure.class);
        verify(courseRewardFailureRepository).save(captor.capture());

        CourseRewardFailure savedFailure = captor.getValue();
        assertEquals(USER_ID, savedFailure.getUserId());
        assertEquals(COURSE_ID, savedFailure.getCourseId());
        assertEquals(COMPLETION_ID, savedFailure.getCompletionId());
        assertEquals(CourseRewardFailureStatus.PENDING, savedFailure.getStatus());
        assertEquals("reward failed", savedFailure.getFailureReason());
        assertEquals(0, savedFailure.getRetryCount());
        assertNotNull(savedFailure.getCreatedAt());
        assertNotNull(savedFailure.getLastFailedAt());
    }

    @Test
    void retriesPendingFailureAndMarksResolvedWhenRewardSucceeds() {
        CourseRewardFailure pendingFailure = failure(CourseRewardFailureStatus.PENDING, 0);

        when(courseRewardFailureRepository.findById(FAILURE_ID))
                .thenReturn(Optional.of(pendingFailure));
        when(courseRewardFailureRepository.save(any(CourseRewardFailure.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = courseRewardFailureService.retryFailure(
                new RetryCourseRewardFailureCommand(FAILURE_ID)
        );

        assertEquals(FAILURE_ID, result.failureId());
        assertEquals(USER_ID, result.userId());
        assertEquals(COURSE_ID, result.courseId());
        assertEquals(COMPLETION_ID, result.completionId());
        assertEquals(CourseRewardFailureStatus.RESOLVED, result.status());
        assertEquals(1, result.retryCount());
        assertNotNull(result.resolvedAt());

        verify(courseRewardUseCase).rewardCourseWithDetails(any());
    }

    @Test
    void retriesPendingFailureAndMarksFailedWhenRewardFails() {
        CourseRewardFailure pendingFailure = failure(CourseRewardFailureStatus.PENDING, 0);
        RuntimeException rewardException = new RuntimeException("reward retry failed");

        when(courseRewardFailureRepository.findById(FAILURE_ID))
                .thenReturn(Optional.of(pendingFailure));
        when(courseRewardFailureRepository.save(any(CourseRewardFailure.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(rewardException)
                .when(courseRewardUseCase)
                .rewardCourseWithDetails(any());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> courseRewardFailureService.retryFailure(
                        new RetryCourseRewardFailureCommand(FAILURE_ID)
                )
        );

        assertSame(rewardException, exception);

        ArgumentCaptor<CourseRewardFailure> captor = ArgumentCaptor.forClass(CourseRewardFailure.class);
        verify(courseRewardFailureRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        List<CourseRewardFailure> savedFailures = captor.getAllValues();
        CourseRewardFailure retryingFailure = savedFailures.get(0);
        CourseRewardFailure failedFailure = savedFailures.get(1);

        assertEquals(CourseRewardFailureStatus.RETRYING, retryingFailure.getStatus());
        assertEquals(1, retryingFailure.getRetryCount());

        assertEquals(CourseRewardFailureStatus.FAILED, failedFailure.getStatus());
        assertEquals(1, failedFailure.getRetryCount());
        assertEquals("reward retry failed", failedFailure.getFailureReason());
    }

    @Test
    void rejectsRetryWhenFailureIsAlreadyResolved() {
        CourseRewardFailure resolvedFailure = failure(CourseRewardFailureStatus.RESOLVED, 1);

        when(courseRewardFailureRepository.findById(FAILURE_ID))
                .thenReturn(Optional.of(resolvedFailure));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> courseRewardFailureService.retryFailure(
                        new RetryCourseRewardFailureCommand(FAILURE_ID)
                )
        );

        assertSame(BenefitErrorCode.COURSE_REWARD_FAILURE_ALREADY_RESOLVED, exception.getErrorCode());
        verifyNoInteractions(courseRewardUseCase);
    }

    @Test
    void rejectsRetryWhenFailureDoesNotExist() {
        when(courseRewardFailureRepository.findById(FAILURE_ID))
                .thenReturn(Optional.empty());

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> courseRewardFailureService.retryFailure(
                        new RetryCourseRewardFailureCommand(FAILURE_ID)
                )
        );

        assertSame(BenefitErrorCode.COURSE_REWARD_FAILURE_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(courseRewardUseCase);
    }

    @Test
    void getsFailuresByStatus() {
        CourseRewardFailure pendingFailure = failure(CourseRewardFailureStatus.PENDING, 0);

        when(courseRewardFailureRepository.findAllByStatus(CourseRewardFailureStatus.PENDING))
                .thenReturn(List.of(pendingFailure));

        var results = courseRewardFailureService.getFailures(CourseRewardFailureStatus.PENDING);

        assertEquals(1, results.size());
        assertEquals(FAILURE_ID, results.get(0).failureId());
        assertEquals(CourseRewardFailureStatus.PENDING, results.get(0).status());
    }

    @Test
    void getsRetryableFailures() {
        CourseRewardFailure failedFailure = failure(CourseRewardFailureStatus.FAILED, 2);

        when(courseRewardFailureRepository.findRetryableFailures(3))
                .thenReturn(List.of(failedFailure));

        var results = courseRewardFailureService.getRetryableFailures(3);

        assertEquals(1, results.size());
        assertEquals(FAILURE_ID, results.get(0).failureId());
        assertEquals(CourseRewardFailureStatus.FAILED, results.get(0).status());
        assertEquals(2, results.get(0).retryCount());
    }

    private CourseRewardFailure failure(
            CourseRewardFailureStatus status,
            int retryCount
    ) {
        LocalDateTime now = LocalDateTime.now();

        return CourseRewardFailure.withId(
                FAILURE_ID,
                USER_ID,
                COURSE_ID,
                COMPLETION_ID,
                status,
                "reward failed",
                retryCount,
                now.minusMinutes(10),
                now.minusMinutes(5),
                status == CourseRewardFailureStatus.RESOLVED ? now : null
        );
    }
}