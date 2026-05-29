package com.kidmily.algoga_server.notification.infrastructure.persistence;

import com.kidmily.algoga_server.notification.domain.model.Notification;
import com.kidmily.algoga_server.notification.domain.repository.NotificationRepository;
import com.kidmily.algoga_server.notification.infrastructure.mapper.NotificationMapper;
import com.kidmily.algoga_server.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository springDataRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notificationMapper.toJpaEntity(notification);
        NotificationJpaEntity saved = springDataRepository.save(entity);
        return notificationMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(Long notificationId) {
        return springDataRepository.findById(notificationId)
                .map(notificationMapper::toDomain);
    }

    @Override
    public Page<Notification> findByUserId(Long userId, Boolean isRead, Pageable pageable) {
        Page<NotificationJpaEntity> entities = (isRead == null)
                ? springDataRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : springDataRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable);
        return entities.map(notificationMapper::toDomain);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataRepository.deleteAllByUserId(userId);
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return springDataRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        springDataRepository.markAllAsRead(userId);
    }

    @Override
    public void deleteById(Long notificationId) {
        springDataRepository.deleteById(notificationId);
    }
}