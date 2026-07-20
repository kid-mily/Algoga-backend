package com.kidmily.algoga_server.notification.application.scheduler;

import com.kidmily.algoga_server.notification.application.port.CoursePort;
import com.kidmily.algoga_server.notification.application.port.EnrollmentPort;
import com.kidmily.algoga_server.notification.domain.event.DdayReminderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentExpiryReminderScheduler {

    private static final int REMIND_DAYS_BEFORE = 7;

    private final EnrollmentPort enrollmentPort;
    private final CoursePort coursePort;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 10 * * *")   // 매일 오전 10시
    @Transactional(readOnly = true)
    public void notifyExpiringEnrollments() {
        // 만료일이 "7일 뒤 하루 전체"에 속하는 수강건
        LocalDateTime start = LocalDate.now().plusDays(REMIND_DAYS_BEFORE).atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<EnrollmentPort.ExpiringEnrollment> targets =
                enrollmentPort.findExpiringBetween(start, end);

        if (targets.isEmpty()) {
            return;
        }

        targets.forEach(target -> eventPublisher.publishEvent(new DdayReminderEvent(
                target.userId(),
                coursePort.getCourseName(target.courseId()),
                target.courseId()
        )));

        log.info("[배치 작업] 수강 기간 만료 {}일 전 알림 {}건 발송", REMIND_DAYS_BEFORE, targets.size());
    }
}