package com.kidmily.algoga_server.notification.application.service;

import com.kidmily.algoga_server.notification.application.usecase.NotificationQueryUseCase;
import com.kidmily.algoga_server.notification.domain.model.Notification;
import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import com.kidmily.algoga_server.notification.presentation.api.response.NotificationItemResponse;
import com.kidmily.algoga_server.notification.presentation.api.response.NotificationListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public NotificationListResponse getNotifications(Long userId, Boolean isRead, int page, int size) {
        log.info("[NotificationQueryService] 알림 목록 조회 - userId: {}, isRead: {}, page: {}, size: {}",
                userId, isRead, page, size);

        // page는 1부터 시작이라 0 기반으로 변환
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Notification> notificationPage = notificationRepository.findByUserId(userId, isRead, pageable);

        long unreadCount = notificationRepository.countUnreadByUserId(userId);

        List<NotificationItemResponse> notifications = notificationPage.getContent().stream()
                .map(n -> new NotificationItemResponse(
                        n.getNotificationId(),
                        n.getType(),
                        n.getMessage(),
                        n.getIsRead(),
                        n.getCreatedAt()
                ))
                .toList();

        return new NotificationListResponse(
                unreadCount,
                unreadCount > 0,
                notifications,
                notificationPage.hasNext(),
                notificationPage.getTotalElements()
        );
    }
}