package com.kidmily.algoga_server.notification.domain.repository;

import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import java.util.Optional;

public interface NotificationSettingRepository {
    NotificationSetting save(NotificationSetting setting);
    Optional<NotificationSetting> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}