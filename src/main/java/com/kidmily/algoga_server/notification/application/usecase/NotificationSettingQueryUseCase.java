package com.kidmily.algoga_server.notification.application.usecase;

import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;


public interface NotificationSettingQueryUseCase {
    NotificationSetting getSetting(Long userId);
}
