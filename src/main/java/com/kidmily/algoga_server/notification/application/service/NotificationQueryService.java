package com.kidmily.algoga_server.notification.application.service;

import com.kidmily.algoga_server.notification.application.usecase.NotificationQueryUseCase;
import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public long getUnreadCount(Long userId) {
        log.info("[NotificationQueryService] 읽지 않은 알림 개수 조회 - userId: {}", userId);
        return notificationRepository.countUnreadByUserId(userId);
    }
}