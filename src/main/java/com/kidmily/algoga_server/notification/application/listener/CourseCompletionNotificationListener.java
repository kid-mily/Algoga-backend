package com.kidmily.algoga_server.notification.application.listener;

import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import com.kidmily.algoga_server.notification.application.port.CoursePort;
import com.kidmily.algoga_server.notification.domain.event.CourseCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseCompletionNotificationListener {

    private final CoursePort coursePort;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCourseCompletion(CourseCompletionCompletedEvent event) {
        try {
            eventPublisher.publishEvent(new CourseCompletedEvent(
                    event.userId(),
                    coursePort.getCourseName(event.courseId())
            ));
        } catch (Exception e) {
            log.error("[Notification] 수강 완료 알림 발행 실패 - userId: {}", event.userId(), e);
        }
    }
}