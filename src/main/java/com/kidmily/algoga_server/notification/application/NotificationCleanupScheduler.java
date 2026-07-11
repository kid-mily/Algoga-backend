package com.kidmily.algoga_server.notification.application;

import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final NotificationRepository notificationRepository;

    // 매일 자정 실행 — 30일 지난 알림 영구 삭제
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldNotifications(threshold);
        if (deleted > 0) {
            log.info("[배치 작업] 30일 경과한 알림 {}건 영구 삭제 완료", deleted);
        }
    }
}