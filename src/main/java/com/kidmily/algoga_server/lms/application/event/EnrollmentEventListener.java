package com.kidmily.algoga_server.lms.application.event;

import com.kidmily.algoga_server.lms.domain.model.Enrollment;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final EnrollmentRepository enrollmentRepository;

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        if (event.paymentType() != PaymentType.LECTURE_ONLY || event.courseId() == null) {
            return;
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(event.userId(), event.courseId())) {
            log.warn("[EnrollmentEventListener] 이미 수강 등록된 강의 - userId: {}, courseId: {}",
                    event.userId(), event.courseId());
            return;
        }

        Enrollment enrollment = Enrollment.create(event.userId(), event.courseId());
        enrollmentRepository.save(enrollment);
        log.info("[EnrollmentEventListener] 수강 등록 완료 - userId: {}, courseId: {}",
                event.userId(), event.courseId());
    }
}