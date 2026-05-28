package com.kidmily.algoga_server.notification.application.listener;

import com.kidmily.algoga_server.notification.domain.event.NotificationEvent;
import com.kidmily.algoga_server.notification.domain.model.Notification;
import com.kidmily.algoga_server.notification.domain.model.NotificationCategory;
import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import com.kidmily.algoga_server.notification.domain.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Async
    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("[NotificationEventListener] 알림 이벤트 수신 - type: {}, receiverId: {}",
                event.getType(), event.getReceiverId());

        // 자기 자신에게 보내는 알림이면 무시 (예: 내 게시글에 내가 댓글)
        if (event.getReceiverId() == null) {
            log.warn("[NotificationEventListener] receiverId가 null이라 알림 생성 안 함");
            return;
        }

        // 알림 설정 확인
        NotificationCategory category = event.getType().getCategory();
        if (!isNotificationEnabled(event.getReceiverId(), category)) {
            log.info("[NotificationEventListener] 알림 설정 OFF - 알림 생성 안 함, userId: {}, category: {}",
                    event.getReceiverId(), category);
            return;
        }

        // 알림 저장
        Notification notification = Notification.create(
                event.getReceiverId(),
                event.getType(),
                event.getMessage()
        );

        notificationRepository.save(notification);

        log.info("[NotificationEventListener] 알림 저장 완료 - userId: {}, type: {}",
                event.getReceiverId(), event.getType());
    }

    private boolean isNotificationEnabled(Long userId, NotificationCategory category) {
        // 필수 알림은 항상 발송
        if (category == NotificationCategory.MANDATORY) {
            return true;
        }

        // 설정이 없으면 기본값(전부 활성) 생성하고 발송
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.createDefault(userId)
                ));

        return setting.isEnabledFor(category);
    }
}