package com.kidmily.algoga_server.notification.application.command;

public record UpdateNotificationSettingCommand(
        Long userId,
        Boolean learningEnabled,
        Boolean qnaEnabled,
        Boolean communityEnabled,
        Boolean noticeEnabled,
        Boolean inquiryEnabled,
        Boolean friendEnabled
) {}