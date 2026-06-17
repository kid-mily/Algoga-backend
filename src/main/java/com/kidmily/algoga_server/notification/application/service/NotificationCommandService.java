package com.kidmily.algoga_server.notification.application.service;

import com.kidmily.algoga_server.notification.application.usecase.NotificationCommandUseCase;
import com.kidmily.algoga_server.notification.domain.model.Notification;
import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import com.kidmily.algoga_server.notification.exception.NotificationErrorCode;
import com.kidmily.algoga_server.notification.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService implements NotificationCommandUseCase {

    private final NotificationRepository notificationRepository;


    @Override
    public void markAsRead(Long userId, Long notificationId) {
        log.info("[NotificationCommandService] 개별 읽음 처리 - userId: {}, notificationId: {}",
                userId, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 본인 알림인지 확인
        if (!notification.getUserId().equals(userId)) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("[NotificationCommandService] 개별 읽음 처리 완료 - notificationId: {}", notificationId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        log.info("[NotificationCommandService] 전체 읽음 처리 - userId: {}", userId);
        notificationRepository.markAllAsRead(userId);
        log.info("[NotificationCommandService] 전체 읽음 처리 완료 - userId: {}", userId);
    }

    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        log.info("[NotificationCommandService] 개별 삭제 - userId: {}, notificationId: {}",
                userId, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notificationRepository.deleteById(notificationId);
        log.info("[NotificationCommandService] 개별 삭제 완료 - notificationId: {}", notificationId);
    }

    @Override
    public void deleteAllNotifications(Long userId) {
        log.info("[NotificationCommandService] 전체 삭제 - userId: {}", userId);
        notificationRepository.deleteAllByUserId(userId);
        log.info("[NotificationCommandService] 전체 삭제 완료 - userId: {}", userId);
    }
}