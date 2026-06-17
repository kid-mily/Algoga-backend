package com.kidmily.algoga_server.lms.application.event;

import com.kidmily.algoga_server.global.event.LecturePaymentCompletedEvent;
import com.kidmily.algoga_server.lms.domain.model.Enrollment;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LmsEnrollmentEventListenerTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private LmsEnrollmentEventListener listener;

    @Test
    void createsEnrollmentFromLecturePaymentDate() {
        LocalDateTime paidAt = LocalDateTime.of(2026, 6, 15, 12, 0);
        LecturePaymentCompletedEvent event = new LecturePaymentCompletedEvent(2L, 54L, paidAt);

        listener.handleLecturePaymentCompleted(event);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().getUserId());
        assertEquals(54L, captor.getValue().getCourseId());
        assertEquals(paidAt, captor.getValue().getEnrolledAt());
        assertEquals(paidAt.plusMonths(6), captor.getValue().getAccessExpiresAt());
    }

    @Test
    void ignoresDuplicateEnrollmentEvent() {
        LecturePaymentCompletedEvent event = new LecturePaymentCompletedEvent(
                2L,
                54L,
                LocalDateTime.of(2026, 6, 15, 12, 0)
        );
        when(enrollmentRepository.existsByUserIdAndCourseId(2L, 54L)).thenReturn(true);

        listener.handleLecturePaymentCompleted(event);

        verify(enrollmentRepository, never()).save(any());
    }
}
