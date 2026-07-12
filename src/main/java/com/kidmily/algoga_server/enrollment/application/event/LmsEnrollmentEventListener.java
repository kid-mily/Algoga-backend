package com.kidmily.algoga_server.enrollment.application.event;

import com.kidmily.algoga_server.global.event.LecturePaymentCompletedEvent;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LmsEnrollmentEventListener {

    private final EnrollmentRepository enrollmentRepository;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLecturePaymentCompleted(LecturePaymentCompletedEvent event) {
        if (enrollmentRepository.existsByUserIdAndCourseId(event.userId(), event.courseId())) {
            log.warn("[LmsEnrollmentEventListener] Already enrolled. userId={}, courseId={}",
                    event.userId(), event.courseId());
            return;
        }

        enrollmentRepository.save(Enrollment.create(event.userId(), event.courseId(), event.paidAt()));
        log.info("[LmsEnrollmentEventListener] Enrollment created. userId={}, courseId={}",
                event.userId(), event.courseId());
    }
}
