package com.kidmily.algoga_server.notification.domain.repository;

import com.kidmily.algoga_server.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(Long notificationId);
    Page<Notification> findByUserId(Long userId, Boolean isRead, Pageable pageable);
    long countUnreadByUserId(Long userId);
    void markAllAsRead(Long userId);
    void deleteById(Long notificationId);
    void deleteAllByUserId(Long userId);
    int deleteOldNotifications(LocalDateTime threshold);
}