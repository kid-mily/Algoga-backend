package com.kidmily.algoga_server.lms.application.event;

import com.kidmily.algoga_server.lms.domain.model.Enrollment;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
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

    private static final String PAYMENT_COMPLETED_EVENT =
            "com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent";

    private final EnrollmentRepository enrollmentRepository;

    @EventListener
    public void handlePaymentCompleted(Object event) {
        if (!PAYMENT_COMPLETED_EVENT.equals(event.getClass().getName())) {
            return;
        }

        Long userId = (Long) invoke(event, "userId");
        Long courseId = (Long) invoke(event, "courseId");
        Object paymentType = invoke(event, "paymentType");

        if (paymentType == null || !"LECTURE_ONLY".equals(paymentType.toString()) || courseId == null) {
            return;
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.warn("[EnrollmentEventListener] Already enrolled. userId={}, courseId={}", userId, courseId);
            return;
        }

        enrollmentRepository.save(Enrollment.create(userId, courseId));
        log.info("[EnrollmentEventListener] Enrollment created. userId={}, courseId={}", userId, courseId);
    }

    private Object invoke(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read payment event: " + methodName, exception);
        }
    }
}
