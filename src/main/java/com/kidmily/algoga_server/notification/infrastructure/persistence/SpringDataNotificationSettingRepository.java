package com.kidmily.algoga_server.notification.infrastructure.persistence;

import com.kidmily.algoga_server.notification.infrastructure.persistence.entity.NotificationSettingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationSettingRepository extends JpaRepository<NotificationSettingJpaEntity, Long> {
    void deleteByUserId(Long userId);

}