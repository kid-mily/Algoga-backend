package com.kidmily.algoga_server.notification.application.usecase;

import com.kidmily.algoga_server.notification.application.command.UpdateNotificationSettingCommand;
import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;

public interface NotificationSettingCommandUseCase {
    NotificationSetting updateSetting(UpdateNotificationSettingCommand command);
}