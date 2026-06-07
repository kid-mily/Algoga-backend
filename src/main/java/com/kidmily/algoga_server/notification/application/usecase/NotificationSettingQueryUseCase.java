package com.kidmily.algoga_server.notification.application.usecase;

import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import com.kidmily.algoga_server.notification.presentation.api.response.NotificationSettingResponse;

public interface NotificationSettingQueryUseCase {
    NotificationSetting getSetting(Long userId);
}
