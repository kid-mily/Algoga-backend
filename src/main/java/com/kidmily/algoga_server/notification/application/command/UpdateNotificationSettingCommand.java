package com.kidmily.algoga_server.notification.application.command;

public record UpdateNotificationSettingCommand(
        Long userId,
        Boolean communityEnabled
) {}